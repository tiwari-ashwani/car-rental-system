package com.carrental.service;


import com.carrental.client.CarRentalPricingClient;
import com.carrental.client.DrivingLicenseClient;
import com.carrental.client.dto.RateResponse;
import com.carrental.dto.*;
import com.carrental.entity.Booking;
import com.carrental.entity.Customer;
import com.carrental.entity.Vehicle;
import com.carrental.exception.*;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.CustomerRepository;
import com.carrental.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleService {

    private final VehicleRepository repository;
    private final BookingRepository carBookingRepository;
    private final CustomerRepository  customerRepository;
    private final DrivingLicenseClient drivingLicenseClient;
    private final CarRentalPricingClient carRentalPricingClient;

    @Transactional
    public VehicleResponse createVehicle(VehicleRequest req) {
        log.info("Creating vehicle vin={}", req.vin());
        if (repository.existsByVin(req.vin())) {
            throw new DuplicateVehicleException("Vehicle with VIN already exists: " + req.vin());
        }
        Vehicle v = Vehicle.builder()
                .type(req.type())
                .segment(req.segment())
                .vin(req.vin())
                .modelYear(req.modelYear())
                .status(req.status())
                .build();
        Vehicle saved = repository.save(v);
        log.debug("Vehicle created id={}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicle(UUID id) {
        log.debug("Fetching vehicle id={}", id);
        return repository.findById(id)
                .map(VehicleService::toResponse)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with id " + id));
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> listVehicles() {
        log.debug("Listing vehicles");
        return repository.findAll().stream().map(VehicleService::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public VehicleResponse updateVehicle(UUID id, VehicleRequest req) {
        log.info("Updating vehicle id={}", id);

        Vehicle existing = repository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with id " + id));

        // Check VIN if it has changed
        if (!req.vin().equals(existing.getVin()) && repository.existsByVin(req.vin())) {
            throw new DuplicateVehicleException("VIN already in use: " + req.vin());
        }

        Vehicle saved = repository.save(updateVehicleDetails(req,  existing));
        log.debug("Vehicle updated successfully: id={}", saved.getId());

        return toResponse(saved);
    }


    @Transactional
    public void deleteVehicle(UUID id) {
        log.info("Deleting vehicle id={}", id);
        if (!repository.existsById(id)) {
            throw new VehicleNotFoundException("Vehicle not found with id " + id);
        }
        repository.deleteById(id);
        log.debug("Deleted vehicle id={}", id);
    }


    @Transactional
    public UUID createBooking(BookingRequest req) {
        log.info("Creating booking for license={} and VIN={}", req.licenseNumber(), req.vin());
        validateReservationDates(req.reservationStartDate(), req.reservationEndDate());
        long rentalDays = calculateInclusiveDays(req.reservationStartDate(), req.reservationEndDate());

        // 2. call driving license API
        Optional<DrivingLicenseClient.LicenseResponse> licenseOpt =
                drivingLicenseClient.getLicenseDetails(req.licenseNumber());

        if (licenseOpt.isEmpty()) {
            log.warn("License validation failed for {}", req.licenseNumber());
            throw new BookingException("Driving license not found: " + req.licenseNumber());
        }

        var license = licenseOpt.get();

        if (license.expiryDate() != null && license.expiryDate().isBefore(LocalDate.now())) {
            throw new BookingException("Driving license must have at least 1 year remaining validity");
        }

        if (license.ownerName() == null || !license.ownerName().equalsIgnoreCase(req.customerName())) {
            throw new InvalidLicenseOwnerNameException(String.format(
                    "Provided name '%s' does not match driving license name", req.customerName()));        }

        // Fetch customer by driving license
        Customer customer = customerRepository
                .findByDrivingLicenseNumber(req.licenseNumber())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found for license " + req.licenseNumber()));

        // Fetch vehicle
        Vehicle vehicle = repository.findByVin(req.vin())
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with VIN " + req.vin()));

        boolean overlap = carBookingRepository.existsOverlappingBookingForVehicle(
                vehicle.getId(),
                req.reservationStartDate(),
                req.reservationEndDate()
        );
        if (overlap) {
            throw new BookingConflictException("Vehicle with VIN " + req.vin() +" is not available for the requested dates");
        }

        // fetch rate and compute total
        Optional<RateResponse> rateRespOpt = carRentalPricingClient.getRateForCategory(req.segment().name());
        RateResponse rateResp = rateRespOpt
                .orElseThrow(() -> new BookingException("Rate not found for category: " + req.segment()));
        BigDecimal total = calculateTotal(rateResp.ratePerDay(), rentalDays);

        //build, persist and return id
        Booking booking = buildBooking(req, license.ownerName(), rentalDays, total);
        booking.setCustomer(customer);
        booking.setVehicle(vehicle);

        Booking saved = carBookingRepository.save(booking);
        log.info("Booking created successfully with id={} for VIN={}", saved.getId(), req.vin());
        return saved.getId();
    }

    private void validateReservationDates(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "reservationStartDate must not be null");
        Objects.requireNonNull(end, "reservationEndDate must not be null");

        if (end.isBefore(start)) {
            throw new BookingException("Reservation end date must be after or equal to start date");
        }
        long days = calculateInclusiveDays(start, end);
        if (days > 30) {
            throw new BookingException("A car cannot be reserved for more than 30 days");
        }
    }

    private long calculateInclusiveDays(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end) + 1; // inclusive
    }

    private String extractOwnerName(String ownerName) {
        if (ownerName == null || ownerName.isBlank()) {
            throw new InvalidLicenseDetailsException("Driving license does not include owner name");
        }
        return ownerName.trim();
    }

    private BigDecimal calculateTotal(BigDecimal ratePerDay, long days) {
        return ratePerDay.multiply(BigDecimal.valueOf(days));
    }

    private Booking buildBooking(
            BookingRequest req,
            String ownerName,
            long rentalDays,
            BigDecimal total
    ) {
        return Booking.builder()
                .licenseNumber(req.licenseNumber())
                .customerName(ownerName)
                .segment(req.segment())
                .startDate(req.reservationStartDate())
                .endDate(req.reservationEndDate())
                .rentalDays(rentalDays)
                .rentalPrice(total)
                .age(req.age())
                .build();
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingDetails (UUID bookingId){
        Booking booking = carBookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        VehicleSegment seg = VehicleSegment.valueOf(booking.getSegment().name());
        return new BookingResponse(
                booking.getId(),
                booking.getLicenseNumber(),
                booking.getCustomerName(),
                booking.getAge(),
                booking.getStartDate(),
                booking.getEndDate(),
                seg,
                booking.getRentalPrice()
        );
    }

    private static VehicleResponse toResponse(Vehicle v) {
        return new VehicleResponse(v.getId(), v.getType(), v.getSegment(), v.getVin(), v.getModelYear(), v.getStatus());
    }

    private Vehicle updateVehicleDetails(VehicleRequest request, Vehicle existing) {
        return existing.toBuilder()
                .type(request.type())
                .segment(request.segment())
                .vin(request.vin())
                .modelYear(request.modelYear())
                .status(request.status())
                .build();
    }
}