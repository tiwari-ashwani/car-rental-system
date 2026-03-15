package com.carrental.controller;

import com.carrental.dto.*;
import com.carrental.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Slf4j
@Validated
public class VehicleController {

    private final VehicleService service;

    /** Create -> POST /api/v1/vehicles */
    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleRequest req) {
        VehicleResponse created = service.createVehicle(req);
        return ResponseEntity.created(URI.create("/api/v1/vehicles/" + created.id())).body(created);
    }

    /** Fetch -> Get /api/v1/vehicles/{id} */
    @GetMapping("{id}")
    public ResponseEntity<VehicleResponse> getVehicle(@PathVariable UUID id) {
        VehicleResponse resp = service.getVehicle(id); // will throw VehicleNotFoundException -> 404 via handler
        return ResponseEntity.ok(resp);
    }

    /** Fetch All -> Get /api/v1/vehicles */
    @GetMapping
    public ResponseEntity<List<VehicleResponse>> listVehicles() {
        return ResponseEntity.ok(service.listVehicles());
    }

    /** Update -> PUT /api/v1/vehicles/{id} */
    @PutMapping("{id}")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @PathVariable UUID id,
            @Valid @RequestBody VehicleRequest req) {
        VehicleResponse updated = service.updateVehicle(id, req);
        return ResponseEntity.ok(updated);
    }

    /** Delete -> DELETE /api/v1/vehicles/{id} */
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable UUID id) {
        service.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path="/bookings",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UUID>  createBooking(@RequestBody @Valid BookingRequest request) {
        var bookingId = service.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingId);
    }

    @GetMapping(path = "/bookings/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookingResponse> getBookingDetails(@PathVariable("id") UUID bookingId) {
        var resp = service.getBookingDetails(bookingId);
        return ResponseEntity.ok(resp);
    }
}