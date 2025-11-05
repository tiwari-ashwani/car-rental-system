package com.carrental.service;

import com.carrental.entity.Customer;
import com.carrental.exception.CustomerNotFoundException;
import com.carrental.exception.DuplicateCustomerException;
import com.carrental.repository.CustomerRepository;
import com.carrental.dto.CustomerRequest;
import com.carrental.dto.CustomerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository repository;

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest req) {
        log.info("Creating customer with email={} drivingLicense={}", req.email(), req.drivingLicenseNumber());
        if (repository.existsByEmail(req.email())) {
            log.warn("Create failed — email already exists: {}", req.email());
            throw new DuplicateCustomerException("Email already in use: " + req.email());
        }
        if (repository.existsByDrivingLicenseNumber(req.drivingLicenseNumber())) {
            log.warn("Create failed — driving license already exists: {}", req.drivingLicenseNumber());
            throw new IllegalArgumentException("Driving license already in use");
        }

        Customer c = Customer.builder()
                .firstName(req.firstName())
                .lastName(req.lastName())
                .age(req.age())
                .email(req.email())
                .drivingLicenseNumber(req.drivingLicenseNumber())
                .phoneNumber(req.phoneNumber())
                .build();

        Customer saved = repository.save(c);
        log.debug("Customer created: id={}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(UUID id) {
        log.debug("Fetching customer id={}", id);
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id " + id));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> listCustomers() {
        log.debug("Listing customers (non-paged)");
        return repository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public CustomerResponse updateCustomer(UUID id, CustomerRequest req) {
        log.info("Updating customer id={}", id);
        Customer existing = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id.toString()));
        // Save the updated customer
        Customer saved = repository.save(updateCustomerDetails(req, existing));
        log.debug("Customer updated successfully: id={}", saved.getId());
        return toResponse(saved);
    }


    @Transactional
    public boolean deleteCustomer(UUID id) {
        log.info("Deleting customer id={}", id);
        if (!repository.existsById(id)) {
            log.warn("Delete attempted for non-existing customer id={}", id);
            return false;
        }
        repository.deleteById(id);
        log.debug("Deleted customer id={}", id);
        return true;
    }

    private CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(
                c.getId(),
                c.getFirstName(),
                c.getLastName(),
                c.getAge(),
                c.getEmail(),
                c.getDrivingLicenseNumber(),
                c.getPhoneNumber()
        );
    }

    private Customer updateCustomerDetails(CustomerRequest request, Customer existing) {
        return existing.toBuilder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .age(request.age())
                .email(request.email())
                .drivingLicenseNumber(request.drivingLicenseNumber())
                .phoneNumber(request.phoneNumber())
                .build();
    }
}