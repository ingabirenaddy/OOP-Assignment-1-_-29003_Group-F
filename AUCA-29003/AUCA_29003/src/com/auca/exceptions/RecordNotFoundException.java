package com.auca.exceptions;

/**
 * Thrown when a search or update refers to an ID that is not stored. Checked: callers must decide how to recover.
 */
public class RecordNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public RecordNotFoundException(String message) {
        super(message);
    }

    public RecordNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
