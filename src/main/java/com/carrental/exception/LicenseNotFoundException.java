package com.carrental.exception;

public class LicenseNotFoundException  extends RuntimeException {
    public LicenseNotFoundException(String message) {
        super(message);
    }
}
