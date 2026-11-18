package com.auca.exceptions;

/**
 * Thrown when a student tries to borrow a book with no available copies. Checked: the borrow workflow must handle it explicitly.
 */
public class BookUnavailableException extends Exception {

    private static final long serialVersionUID = 1L;

    public BookUnavailableException(String message) {
        super(message);
    }

    public BookUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
