package com.carrental.exception;

public class InvalidLicenseDetailsException extends RuntimeException {

    public InvalidLicenseDetailsException(String message) {
        super(message);
    }
}
