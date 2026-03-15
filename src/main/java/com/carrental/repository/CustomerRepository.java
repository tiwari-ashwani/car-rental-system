package com.carrental.repository;

import com.carrental.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByDrivingLicenseNumber(String drivingLicenseNumber);

    boolean existsByEmail(String email);

    boolean existsByDrivingLicenseNumber(String drivingLicenseNumber);
}
