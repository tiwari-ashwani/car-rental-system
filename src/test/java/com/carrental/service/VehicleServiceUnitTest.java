package com.carrental.service;

import com.carrental.client.CarRentalPricingClient;
import com.carrental.client.DrivingLicenseClient;
import com.carrental.client.dto.LicenseResponse;
import com.carrental.client.dto.RateResponse;
import com.carrental.dto.*;
import com.carrental.entity.*;
import com.carrental.exception.*;
import com.carrental.repository.*;
import com.carrental.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import com.carrental.client.DrivingLicenseClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class VehicleServiceUnitTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private DrivingLicenseClient drivingLicenseClient;

    @Mock
    private CarRentalPricingClient carRentalPricingClient;

    @Captor
    private ArgumentCaptor<Booking> bookingCaptor;

    private VehicleService vehicleService;

    private final UUID sampleId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleService(
                vehicleRepository,
                bookingRepository,
                customerRepository,
                drivingLicenseClient,
                carRentalPricingClient
        );
    }

    @Test
    void createBooking_success() {
        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = LocalDate.now().plusDays(4); // inclusive 3 days
        long expectedDays = 3;

        var req = new BookingRequest(
                "DL-ABC",
                "John Doe",
                30,
                start,
                end,
                VehicleSegment.MEDIUM,
                "VIN123"
        );

        // License API returns valid license
        DrivingLicenseClient.LicenseResponse  licenseResp = new DrivingLicenseClient.LicenseResponse("John Doe", LocalDate.now().plusYears(2));
        when(drivingLicenseClient.getLicenseDetails("DL-ABC"))
                .thenReturn(Optional.of(licenseResp));

        // Pricing API returns valid rate
        RateResponse rateResp = new RateResponse("MEDIUM", new BigDecimal("10.00"));
        when(carRentalPricingClient.getRateForCategory("MEDIUM"))
                .thenReturn(Optional.of(rateResp));

        // Customer lookup
        Customer customer = Customer.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .drivingLicenseNumber("DL-ABC")
                .age(30)
                .build();
        when(customerRepository.findByDrivingLicenseNumber("DL-ABC"))
                .thenReturn(Optional.of(customer));

        // Vehicle lookup
        Vehicle vehicle = Vehicle.builder()
                .id(UUID.randomUUID())
                .vin("VIN123")
                .segment(VehicleSegment.MEDIUM)
                .status(VehicleStatus.AVAILABLE)
                .build();
        when(vehicleRepository.findByVin("VIN123")).thenReturn(Optional.of(vehicle));

        // No booking overlap
        when(bookingRepository.existsOverlappingBookingForVehicle(any(), any(), any())).thenReturn(false);

        // Save stub
        Booking saved = Booking.builder()
                .id(sampleId)
                .licenseNumber("DL-ABC")
                .customerName("John Doe")
                .age(30)
                .startDate(start)
                .endDate(end)
                .segment(VehicleSegment.MEDIUM)
                .rentalPrice(new BigDecimal("30.00"))
                .rentalDays(expectedDays)
                .customer(customer)
                .vehicle(vehicle)
                .build();

        when(bookingRepository.save(any(Booking.class))).thenReturn(saved);

        // Act
        UUID bookingId = vehicleService.createBooking(req);

        // Assert
        assertEquals(sampleId, bookingId);
        verify(bookingRepository).save(bookingCaptor.capture());
        Booking captured = bookingCaptor.getValue();
        assertEquals("DL-ABC", captured.getLicenseNumber());
        assertEquals("John Doe", captured.getCustomerName());
        assertEquals(expectedDays, captured.getRentalDays());
        assertEquals(new BigDecimal("30.00"), captured.getRentalPrice());
    }


    @Test
    void createBooking_licenseExpired_throwsBookingException() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(2);
        var req = new BookingRequest("DL-EXP", "X", 20, start, end, VehicleSegment.COMPACT, "VIN123");

        DrivingLicenseClient.LicenseResponse licenseResp =
                new DrivingLicenseClient.LicenseResponse("X", LocalDate.now().minusDays(1)); // expired
        when(drivingLicenseClient.getLicenseDetails("DL-EXP"))
                .thenReturn(Optional.of(licenseResp));

        assertThrows(BookingException.class, () -> vehicleService.createBooking(req));
    }


    @Test
    void createBooking_ownerNameMissing_throwsInvalidLicenseOwnerNameException() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(2);
        var req = new BookingRequest("DL-OWN", "X", 20, start, end, VehicleSegment.COMPACT, "VIN123");

        DrivingLicenseClient.LicenseResponse licenseResp =
                new DrivingLicenseClient.LicenseResponse("   ", LocalDate.now().plusYears(2));
        when(drivingLicenseClient.getLicenseDetails("DL-OWN"))
                .thenReturn(Optional.of(licenseResp));

        assertThrows(InvalidLicenseOwnerNameException.class, () -> vehicleService.createBooking(req));
    }

    @Test
    void createBooking_nameMismatch_throwsInvalidLicenseOwnerNameException() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(2);
        var req = new BookingRequest("DL-OWN", "Provided Name", 20, start, end, VehicleSegment.COMPACT, "VIN123");

        DrivingLicenseClient.LicenseResponse licenseResp =
                new DrivingLicenseClient.LicenseResponse("Different Name", LocalDate.now().plusYears(2));
        when(drivingLicenseClient.getLicenseDetails("DL-OWN"))
                .thenReturn( Optional.of(licenseResp));

        assertThrows(InvalidLicenseOwnerNameException.class, () -> vehicleService.createBooking(req));
    }



    @Test
    void createBooking_rateMissing_throwsBookingException() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(2);
        var req = new BookingRequest("DL-1", "John", 20, start, end, VehicleSegment.MEDIUM, "VIN11123");

        DrivingLicenseClient.LicenseResponse licenseResp =
                new DrivingLicenseClient.LicenseResponse("John", LocalDate.now().plusYears(2));
        when(drivingLicenseClient.getLicenseDetails("DL-1"))
                .thenReturn( Optional.of(licenseResp));

        Customer customer = Customer.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .drivingLicenseNumber("DL-1")
                .age(30)
                .build();
        when(customerRepository.findByDrivingLicenseNumber("DL-1")).thenReturn(Optional.of(customer));

        Vehicle vehicle = Vehicle.builder()
                .id(UUID.randomUUID())
                .vin("VIN11123")
                .segment(VehicleSegment.MEDIUM)
                .status(VehicleStatus.AVAILABLE)
                .build();
        when(vehicleRepository.findByVin("VIN11123")).thenReturn(Optional.of(vehicle));

        when(bookingRepository.existsOverlappingBookingForVehicle(any(), any(), any())).thenReturn(false);

        when(carRentalPricingClient.getRateForCategory("MEDIUM"))
                .thenReturn(Optional.empty());

        assertThrows(BookingException.class, () -> vehicleService.createBooking(req));
    }





}