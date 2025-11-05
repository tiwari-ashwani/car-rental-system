package com.carrental.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


import com.carrental.dto.CustomerRequest;
import com.carrental.dto.CustomerResponse;
import com.carrental.entity.Customer;
import com.carrental.exception.CustomerNotFoundException;
import com.carrental.exception.DuplicateCustomerException;
import com.carrental.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceUnitTest {

    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private CustomerService customerService;

    @Captor
    private ArgumentCaptor<Customer> customerCaptor;

    private UUID sampleId;

    @BeforeEach
    void setUp() {
        sampleId = UUID.randomUUID();
    }

    @Test
    void createCustomer_success() {
        // arrange
        var req = new CustomerRequest(
                "John", "Doe", 30, "john.doe@example.com", "DL-123", "1234567890"
        );

        // when repository checks for uniqueness
        when(repository.existsByEmail(req.email())).thenReturn(false);
        when(repository.existsByDrivingLicenseNumber(req.drivingLicenseNumber())).thenReturn(false);

        // simulate saved entity with id assigned
        var saved = Customer.builder()
                .id(sampleId)
                .firstName(req.firstName())
                .lastName(req.lastName())
                .age(req.age())
                .email(req.email())
                .drivingLicenseNumber(req.drivingLicenseNumber())
                .phoneNumber(req.phoneNumber())
                .build();

        when(repository.save(any(Customer.class))).thenReturn(saved);

        // act
        CustomerResponse resp = customerService.createCustomer(req);

        // assert
        assertNotNull(resp);
        assertEquals(sampleId, resp.id());
        assertEquals("John", resp.firstName());
        assertEquals("Doe", resp.lastName());
        assertEquals(30, resp.age());
        assertEquals("john.doe@example.com", resp.email());
        assertEquals("DL-123", resp.drivingLicenseNumber());
        assertEquals("1234567890", resp.phoneNumber());

        // verify repository.save was called with expected fields
        verify(repository).save(customerCaptor.capture());
        Customer toSave = customerCaptor.getValue();
        assertNull(toSave.getId()); // typically id not set before save
        assertEquals("John", toSave.getFirstName());
        assertEquals("john.doe@example.com", toSave.getEmail());
    }

    @Test
    void createCustomer_duplicateEmail_throwsDuplicateCustomerException() {
        var req = new CustomerRequest("A", "B", 25, "dup@example.com", "DL-1", "000");

        when(repository.existsByEmail(req.email())).thenReturn(true);

        DuplicateCustomerException ex = assertThrows(DuplicateCustomerException.class, () ->
                customerService.createCustomer(req)
        );

        assertTrue(ex.getMessage().contains("Email already in use"));
        verify(repository, never()).save(any());
    }

    @Test
    void createCustomer_duplicateDrivingLicense_throwsIllegalArgumentException() {
        var req = new CustomerRequest("A", "B", 25, "unique@example.com", "DL-99", "000");

        when(repository.existsByEmail(req.email())).thenReturn(false);
        when(repository.existsByDrivingLicenseNumber(req.drivingLicenseNumber())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                customerService.createCustomer(req)
        );

        assertTrue(ex.getMessage().contains("Driving license already in use"));
        verify(repository, never()).save(any());
    }

    @Test
    void getCustomerById_found_returnsResponse() {
        var customer = Customer.builder()
                .id(sampleId)
                .firstName("Alice")
                .lastName("Smith")
                .age(40)
                .email("alice@example.com")
                .drivingLicenseNumber("DL-A")
                .phoneNumber("111")
                .build();

        when(repository.findById(sampleId)).thenReturn(Optional.of(customer));

        CustomerResponse resp = customerService.getCustomerById(sampleId);

        assertNotNull(resp);
        assertEquals(sampleId, resp.id());
        assertEquals("Alice", resp.firstName());
    }

    @Test
    void getCustomerById_notFound_throwsCustomerNotFoundException() {
        when(repository.findById(sampleId)).thenReturn(Optional.empty());

        CustomerNotFoundException ex = assertThrows(CustomerNotFoundException.class, () ->
                customerService.getCustomerById(sampleId)
        );

        assertTrue(ex.getMessage().contains(sampleId.toString()));
    }

    @Test
    void listCustomers_returnsAll() {
        var c1 = Customer.builder().id(UUID.randomUUID()).firstName("A").lastName("A").age(20).email("a@x").drivingLicenseNumber("DL1").phoneNumber("1").build();
        var c2 = Customer.builder().id(UUID.randomUUID()).firstName("B").lastName("B").age(21).email("b@x").drivingLicenseNumber("DL2").phoneNumber("2").build();

        when(repository.findAll()).thenReturn(List.of(c1, c2));

        List<CustomerResponse> responses = customerService.listCustomers();

        assertEquals(2, responses.size());
        assertTrue(responses.stream().anyMatch(r -> r.email().equals("a@x")));
        assertTrue(responses.stream().anyMatch(r -> r.email().equals("b@x")));
    }

    @Test
    void updateCustomer_existing_returnsUpdated() {
        UUID id = sampleId;

        Customer existing = Customer.builder()
                .id(id)
                .firstName("OldFirst")
                .lastName("OldLast")
                .age(50)
                .email("old@example.com")
                .drivingLicenseNumber("OLDDL")
                .phoneNumber("999")
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(existing));

        var updateReq = new CustomerRequest(
                "NewFirst",
                "NewLast",
                55,
                "new@example.com",
                "DL123123123",
                "777"
        );

        // simulate save returning updated entity
        Customer saved = Customer.builder()
                .id(id)
                .firstName("NewFirst")
                .lastName("OldLast")
                .age(55)
                .email("new@example.com")
                .drivingLicenseNumber("OLDDL")
                .phoneNumber("777")
                .build();

        when(repository.save(any(Customer.class))).thenReturn(saved);

        CustomerResponse resp = customerService.updateCustomer(id, updateReq);

        assertEquals("NewFirst", resp.firstName());
        assertEquals(55, resp.age());
        assertEquals("new@example.com", resp.email());

        verify(repository).save(customerCaptor.capture());
        Customer toSave = customerCaptor.getValue();
        assertEquals("NewFirst", toSave.getFirstName());
        assertEquals("NewLast", toSave.getLastName()); // unchanged
    }

    @Test
    void deleteCustomer_exists_deletesAndReturnsTrue() {
        UUID id = sampleId;
        when(repository.existsById(id)).thenReturn(true);

        boolean result = customerService.deleteCustomer(id);

        assertTrue(result);
        verify(repository).deleteById(id);
    }

    @Test
    void deleteCustomer_notExists_returnsFalse() {
        UUID id = sampleId;
        when(repository.existsById(id)).thenReturn(false);

        boolean result = customerService.deleteCustomer(id);

        assertFalse(result);
        verify(repository, never()).deleteById(any());
    }
}