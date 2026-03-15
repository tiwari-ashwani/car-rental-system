package com.carrental.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateVehicleException extends RuntimeException {
    public DuplicateVehicleException(String message) { super(message); }
}
