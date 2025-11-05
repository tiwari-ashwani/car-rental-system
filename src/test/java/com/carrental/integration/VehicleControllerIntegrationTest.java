package com.carrental.integration;

import com.carrental.controller.VehicleController;
import com.carrental.dto.*;
import com.carrental.service.VehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class VehicleControllerIntegrationTest {

    @Autowired
    private VehicleController vehicleController;

    private MockMvc mockMvc;

    @MockBean
    private VehicleService vehicleService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(vehicleController).build();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void testCreateVehicle() throws Exception {
        UUID id = UUID.randomUUID();
        VehicleRequest request = new VehicleRequest(
                "Honda", VehicleSegment.COMPACT, "VIN123456789", 2021, VehicleStatus.AVAILABLE
        );
        VehicleResponse response = new VehicleResponse(
                id, "Honda", VehicleSegment.COMPACT, "VIN123456789", 2021, VehicleStatus.AVAILABLE
        );

        when(vehicleService.createVehicle(any(VehicleRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value("Honda"))
                .andExpect(jsonPath("$.segment").value("COMPACT"))
                .andExpect(jsonPath("$.vin").value("VIN123456789"))
                .andExpect(jsonPath("$.modelYear").value(2021))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void testGetVehicle() throws Exception {
        UUID id = UUID.randomUUID();
        VehicleResponse response = new VehicleResponse(
                id, "Toyota", VehicleSegment.MEDIUM, "VIN987654321", 2020, VehicleStatus.RENTED
        );

        when(vehicleService.getVehicle(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/vehicles/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value("Toyota"))
                .andExpect(jsonPath("$.segment").value("MEDIUM"))
                .andExpect(jsonPath("$.vin").value("VIN987654321"))
                .andExpect(jsonPath("$.modelYear").value(2020))
                .andExpect(jsonPath("$.status").value("RENTED"));
    }

    @Test
    void testListVehicles() throws Exception {
        VehicleResponse v1 = new VehicleResponse(UUID.randomUUID(), "Honda", VehicleSegment.COMPACT, "VIN123", 2021, VehicleStatus.AVAILABLE);
        VehicleResponse v2 = new VehicleResponse(UUID.randomUUID(), "Toyota", VehicleSegment.MEDIUM, "VIN456", 2020, VehicleStatus.RENTED);

        when(vehicleService.listVehicles()).thenReturn(List.of(v1, v2));

        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testUpdateVehicle() throws Exception {
        UUID id = UUID.randomUUID();
        VehicleRequest updateRequest = new VehicleRequest(
                "UpdatedType",
                VehicleSegment.LUXURY,
                "VIN999999",
                2022,
                VehicleStatus.MAINTENANCE
        );
        VehicleResponse updatedResponse = new VehicleResponse(
                id, "UpdatedType", VehicleSegment.LUXURY, "VIN999999", 2022, VehicleStatus.MAINTENANCE
        );

        when(vehicleService.updateVehicle(any(UUID.class), any(VehicleRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/vehicles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("UpdatedType"))
                .andExpect(jsonPath("$.segment").value("LUXURY"))
                .andExpect(jsonPath("$.vin").value("VIN999999"))
                .andExpect(jsonPath("$.modelYear").value(2022))
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));
    }

    @Test
    void testDeleteVehicle() throws Exception {
        UUID id = UUID.randomUUID();

        // No exception expected
        mockMvc.perform(delete("/api/v1/vehicles/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void testCreateBooking() throws Exception {
        UUID bookingId = UUID.randomUUID();
        BookingRequest request = new BookingRequest(
                "DL123456789", "John Doe", 30,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 10),
                VehicleSegment.COMPACT,
                "VIN123456789"
        );
        when(vehicleService.createBooking(any(BookingRequest.class))).thenReturn(bookingId);

        mockMvc.perform(post("/api/v1/vehicles/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("\"" + bookingId + "\""));
    }

    @Test
    void testGetBookingDetails() throws Exception {
        UUID bookingId = UUID.randomUUID();
        BookingResponse response = new BookingResponse(
                bookingId,
                "DL123456789",
                "John Doe",
                30,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 10),
                VehicleSegment.COMPACT,
                new BigDecimal("1000.00")
        );

        when(vehicleService.getBookingDetails(bookingId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/vehicles/bookings/{id}", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(bookingId.toString()))
                .andExpect(jsonPath("$.licenseNumber").value("DL123456789"))
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.reservationStartDate[0]").value(2025))
                .andExpect(jsonPath("$.reservationStartDate[1]").value(1))
                .andExpect(jsonPath("$.reservationStartDate[2]").value(1))
                .andExpect(jsonPath("$.reservationEndDate[0]").value(2025))
                .andExpect(jsonPath("$.reservationEndDate[1]").value(1))
                .andExpect(jsonPath("$.reservationEndDate[2]").value(10))
                .andExpect(jsonPath("$.segment").value("COMPACT"))
                .andExpect(jsonPath("$.rentalPrice").value(1000.00));
    }
}
