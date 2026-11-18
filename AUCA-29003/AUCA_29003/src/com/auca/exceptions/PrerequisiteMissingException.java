package com.auca.exceptions;

/**
 * Thrown when a workflow needs a related record that does not exist yet (e.g. enrolling with no Course). Checked: the caller must offer guided registration or return to the menu.
 */
public class PrerequisiteMissingException extends Exception {

    private static final long serialVersionUID = 1L;

    public PrerequisiteMissingException(String message) {
        super(message);
    }

    public PrerequisiteMissingException(String message, Throwable cause) {
        super(message, cause);
    }
}
