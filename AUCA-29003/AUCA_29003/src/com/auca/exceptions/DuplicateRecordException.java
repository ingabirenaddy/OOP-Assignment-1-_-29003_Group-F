package com.auca.exceptions;

/**
 * Thrown when a unique value (ID, student number, course code, receipt number, ISBN...) already exists, or when an active enrollment/loan would be duplicated. Unchecked because it is raised during field entry, like ValidationException.
 */
public class DuplicateRecordException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateRecordException(String message) {
        super(message);
    }

    public DuplicateRecordException(String message, Throwable cause) {
        super(message, cause);
    }
}
