package com.carrental.integration;

import com.carrental.controller.CustomerController;
import com.carrental.dto.CustomerRequest;
import com.carrental.dto.CustomerResponse;
import com.carrental.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class CustomerControllerIntegrationTest {

    @Autowired
    private CustomerController customerController;

    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
    }

    @Test
    void testCreateCustomer() throws Exception {
        UUID id = UUID.randomUUID();
        CustomerRequest request = new CustomerRequest(
                "John", "Doe", 30, "john.doe@example.com", "DL123456", "+1234567890"
        );
        CustomerResponse response = new CustomerResponse(
                id, "John", "Doe", 30, "john.doe@example.com", "DL123456", "+1234567890"
        );

        when(customerService.createCustomer(any(CustomerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.drivingLicenseNumber").value("DL123456"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"));
    }

    @Test
    void testGetCustomer() throws Exception {
        UUID id = UUID.randomUUID();
        CustomerResponse response = new CustomerResponse(
                id, "Jane", "Smith", 25, "jane.smith@example.com", "DL987654", "+1987654321"
        );

        when(customerService.getCustomerById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.age").value(25))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"))
                .andExpect(jsonPath("$.drivingLicenseNumber").value("DL987654"))
                .andExpect(jsonPath("$.phoneNumber").value("+1987654321"));
    }

    @Test
    void testListCustomers() throws Exception {
        CustomerResponse c1 = new CustomerResponse(UUID.randomUUID(), "Alice", "Brown", 28, "alice.brown@example.com", "DL111111", "+1122334455");
        CustomerResponse c2 = new CustomerResponse(UUID.randomUUID(), "Bob", "Green", 35, "bob.green@example.com", "DL222222", "+2233445566");

        when(customerService.listCustomers()).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testUpdateCustomer() throws Exception {
        UUID id = UUID.randomUUID();
        CustomerRequest updateRequest = new CustomerRequest(
                "UpdatedFirstName",
                "UpdatedLastName",
                40,
                "updated.email@example.com",
                "DL999999",
                "+10987654321"
        );
        CustomerResponse updatedResponse = new CustomerResponse(
                id, "UpdatedFirstName", "UpdatedLastName", 40, "updated.email@example.com", "DL999999", "+10987654321"
        );

        when(customerService.updateCustomer(any(UUID.class), any(CustomerRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UpdatedFirstName"))
                .andExpect(jsonPath("$.lastName").value("UpdatedLastName"))
                .andExpect(jsonPath("$.age").value(40))
                .andExpect(jsonPath("$.email").value("updated.email@example.com"))
                .andExpect(jsonPath("$.drivingLicenseNumber").value("DL999999"))
                .andExpect(jsonPath("$.phoneNumber").value("+10987654321"));
    }

    @Test
    void testDeleteCustomer() throws Exception {
        UUID id = UUID.randomUUID();

        // Mock delete success
        when(customerService.deleteCustomer(id)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/customers/{id}", id))
                .andExpect(status().isNoContent());

        // Mock delete failure (customer not found)
        when(customerService.deleteCustomer(id)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/customers/{id}", id))
                .andExpect(status().isNotFound());
    }
}
