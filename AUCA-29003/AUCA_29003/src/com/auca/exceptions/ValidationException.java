package com.auca.exceptions;

/**
 * Thrown when entered or stored data breaks a validation rule (blank text, bad email, out-of-range value...). Unchecked so it can be raised inside setters, validators and lambda-based form field parsers.
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
