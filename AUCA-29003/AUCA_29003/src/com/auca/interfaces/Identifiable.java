package com.auca.interfaces;

import com.auca.exceptions.ValidationException;

/**
 * Contract for every record that can be found by a unique identifier.
 * The six attributes are public static final constants (shared by all
 * implementing classes), not per-object state.
 */
public interface Identifiable {

    public static final String ID_PREFIX = "AUCA";
    public static final int MIN_ID_LENGTH = 4;
    public static final int MAX_ID_LENGTH = 30;
    public static final String ID_SEPARATOR = "-";
    public static final String DEFAULT_STATUS = "ACTIVE";
    public static final int ENTITY_VERSION = 1;

    /** @return the unique identifier of this record. */
    String getId();

    /** @return a short human-readable name for lists and selections. */
    String getDisplayName();

    /** @return true when the record is currently active/usable. */
    boolean isActive();

    /**
     * Shared ID rule used by every class and by the console forms:
     * not blank, MIN_ID_LENGTH..MAX_ID_LENGTH characters, letters, digits, '-' or '_'.
     * IDs are normalised to upper case.
     */
    static String validateId(String raw, String fieldName) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required and cannot be blank.");
        }
        String id = raw.trim().toUpperCase();
        if (id.length() < MIN_ID_LENGTH || id.length() > MAX_ID_LENGTH) {
            throw new ValidationException(fieldName + " must be " + MIN_ID_LENGTH + "-" + MAX_ID_LENGTH
                    + " characters (you entered " + id.length() + ").");
        }
        if (!id.matches("[A-Z0-9_-]+")) {
            throw new ValidationException(fieldName + " may contain only letters, digits, '-' or '_'.");
        }
        return id;
    }

    /** Builds an institutional reference such as AUCA-STU0001-V1. */
    static String referenceOf(Identifiable record) {
        return ID_PREFIX + ID_SEPARATOR + record.getId() + ID_SEPARATOR + "V" + ENTITY_VERSION;
    }
}
