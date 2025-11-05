package com.carrental.exception;

public class InvalidLicenseOwnerNameException extends RuntimeException {

    public InvalidLicenseOwnerNameException(String message) {
        super(message);
    }
}
