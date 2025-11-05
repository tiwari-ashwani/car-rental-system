package com.carrental.controller;

import com.carrental.dto.*;
import com.carrental.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.carrental.dto.VehicleSegment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleControllerTest {

    @Mock
    private VehicleService service;

    @InjectMocks
    private VehicleController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateVehicle() {
        VehicleRequest request = new VehicleRequest
                ("Toyota", VehicleSegment.COMPACT, "VIN1", 2022, VehicleStatus.AVAILABLE);
        VehicleResponse response = new VehicleResponse(UUID.randomUUID(), "Toyota", VehicleSegment.COMPACT, "VIN11", 2022, VehicleStatus.MAINTENANCE);

        when(service.createVehicle(request)).thenReturn(response);

        ResponseEntity<VehicleResponse> result = controller.createVehicle(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
        assertTrue(result.getHeaders().getLocation().toString().contains(response.id().toString()));

        verify(service, times(1)).createVehicle(request);
    }

    @Test
    void testGetVehicle() {
        UUID id = UUID.randomUUID();
        VehicleResponse response = new VehicleResponse(UUID.randomUUID(), "Toyota", VehicleSegment.COMPACT, "VIN11", 2022, VehicleStatus.MAINTENANCE);

        when(service.getVehicle(id)).thenReturn(response);

        ResponseEntity<VehicleResponse> result = controller.getVehicle(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(service, times(1)).getVehicle(id);
    }

    @Test
    void testListVehicles() {
        VehicleResponse v1 = new VehicleResponse(UUID.randomUUID(), "Toyota", VehicleSegment.COMPACT, "VIN11", 2022, VehicleStatus.MAINTENANCE);
        VehicleResponse v2 = new VehicleResponse(UUID.randomUUID(), "Merc", VehicleSegment.COMPACT, "VIN11", 2022, VehicleStatus.MAINTENANCE);

        when(service.listVehicles()).thenReturn(List.of(v1, v2));

        ResponseEntity<List<VehicleResponse>> result = controller.listVehicles();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
        verify(service, times(1)).listVehicles();
    }

    @Test
    void testUpdateVehicle() {
        UUID id = UUID.randomUUID();
        VehicleRequest request = new VehicleRequest( "Honda",  VehicleSegment.ECONOMY,  "VIN123", 2021,  VehicleStatus.OUT_OF_SERVICE);
        VehicleResponse updated = new VehicleResponse(id, "Honda", VehicleSegment.ECONOMY, "VIN123", 2023,VehicleStatus.MAINTENANCE);

        when(service.updateVehicle(id, request)).thenReturn(updated);

        ResponseEntity<VehicleResponse> result = controller.updateVehicle(id, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(updated, result.getBody());
        verify(service, times(1)).updateVehicle(id, request);
    }

    @Test
    void testDeleteVehicle() {
        UUID id = UUID.randomUUID();

        doNothing().when(service).deleteVehicle(id);

        ResponseEntity<Void> result = controller.deleteVehicle(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(service, times(1)).deleteVehicle(id);
    }

    @Test
    void testCreateBooking() {
        BookingRequest request = new BookingRequest("DL123456789", "Rob", 34, LocalDate.now(), LocalDate.now().plusMonths(2), VehicleSegment.COMMERCIAL, "VIN123");
        UUID bookingId = UUID.randomUUID();

        when(service.createBooking(request)).thenReturn(bookingId);

        ResponseEntity<UUID> result = controller.createBooking(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(bookingId, result.getBody());
        verify(service, times(1)).createBooking(request);
    }

    @Test
    void testGetBookingDetails() {
        UUID bookingId = UUID.randomUUID();
        BookingResponse response = new BookingResponse(bookingId, "DL123456789", "ROB JAN", 45, LocalDate.now(), LocalDate.now().plusMonths(2), VehicleSegment.COMMERCIAL, BigDecimal.valueOf(999));

        when(service.getBookingDetails(bookingId)).thenReturn(response);

        ResponseEntity<BookingResponse> result = controller.getBookingDetails(bookingId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(service, times(1)).getBookingDetails(bookingId);
    }
}