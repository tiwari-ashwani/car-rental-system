package com.carrental.controller;

import com.carrental.dto.CustomerRequest;
import com.carrental.dto.CustomerResponse;
import com.carrental.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CustomerController {

    private final CustomerService service;

    /** Create -> POST /api/v1/customers */
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest req) {
        CustomerResponse created = service.createCustomer(req);
        // Location header points to newly created resource
        return ResponseEntity
                .created(URI.create("/api/v1/customers/" + created.id()))
                .body(created);
    }

    /** Get single -> GET /api/v1/customers/{id} */
    @GetMapping("{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getCustomerById(id));
    }

    /** List -> GET /api/v1/customers */
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> listCustomers() {
        return ResponseEntity.ok(service.listCustomers());
    }

    /** Full update -> PUT /api/v1/customers/{id} */
    @PutMapping("{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CustomerRequest req) {
        CustomerResponse updated = service.updateCustomer(id, req);
        return ResponseEntity.ok(updated);
    }


    /** Delete -> DELETE /api/v1/customers/{id} */
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable("id") UUID id) {
        boolean deleted = service.deleteCustomer(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}