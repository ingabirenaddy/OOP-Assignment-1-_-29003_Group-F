package com.auca.interfaces;

import com.auca.exceptions.ValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

/**
 * Contract for every record that can be registered, validated and printed.
 * The static methods are the single shared set of validation rules: the domain
 * classes use them in validate() and the console forms use them field by field,
 * so the same rule is never written twice.
 */
public interface Registrable {

    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 60;
    public static final int MAX_EMAIL_LENGTH = 100;
    public static final int MIN_PHONE_LENGTH = 10;
    public static final int MAX_PHONE_LENGTH = 15;
    /** 'uuuu' (not 'yyyy') is required for STRICT resolving, which rejects dates such as 2025-02-30. */
    public static final String DATE_PATTERN = "uuuu-MM-dd";

    /** Checks every attribute and relationship; throws on the first broken rule. */
    void validate() throws ValidationException;

    /** Prints a complete, labelled description of the record. */
    void printDetails();

    /** @return a one-line summary used in lists. */
    String getRegistrationSummary();

    // ------------------------------------------------------------------ text rules

    static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(field + " is required and cannot be blank.");
        }
        return value.trim();
    }

    static String validateLength(String value, String field, int min, int max) {
        String text = requireText(value, field);
        if (text.length() < min || text.length() > max) {
            throw new ValidationException(field + " must be " + min + "-" + max
                    + " characters (you entered " + text.length() + ").");
        }
        return text;
    }

    /** Names, titles and short labels: MIN_NAME_LENGTH..MAX_NAME_LENGTH characters. */
    static String validateName(String value, String field) {
        return validateLength(value, field, MIN_NAME_LENGTH, MAX_NAME_LENGTH);
    }

    /** Longer texts (program name, course/book title, advisor name): 2..120 characters. */
    static String validateLongText(String value, String field) {
        return validateLength(value, field, MIN_NAME_LENGTH, MAX_NAME_LENGTH * 2);
    }

    /** Codes such as department code, course code, room or office number (upper case). */
    static String validateCode(String value, String field, int min, int max) {
        String code = requireText(value, field).toUpperCase();
        if (!code.matches("[A-Z0-9-]{" + min + "," + max + "}")) {
            throw new ValidationException(field + " must be " + min + "-" + max
                    + " letters, digits or hyphens, without spaces.");
        }
        return code;
    }

    static String validateEmail(String value, String field) {
        String email = requireText(value, field).toLowerCase();
        if (email.length() > MAX_EMAIL_LENGTH) {
            throw new ValidationException(field + " cannot be longer than " + MAX_EMAIL_LENGTH + " characters.");
        }
        if (!email.matches("^[a-z0-9._%+-]+@[a-z0-9-]+(\\.[a-z0-9-]+)*\\.[a-z]{2,}$")) {
            throw new ValidationException(field + " must be a valid email address (example: name@auca.ac.rw).");
        }
        return email;
    }

    static String validatePhone(String value, String field) {
        String phone = requireText(value, field).replace(" ", "");
        if (!phone.matches("\\+?\\d{" + MIN_PHONE_LENGTH + "," + MAX_PHONE_LENGTH + "}")) {
            throw new ValidationException(field + " must contain " + MIN_PHONE_LENGTH + "-" + MAX_PHONE_LENGTH
                    + " digits, optionally starting with + (example: 0781234567).");
        }
        return phone;
    }

    /** ISBN-10 or ISBN-13; hyphens and spaces are removed before checking. */
    static String validateIsbn(String value, String field) {
        String isbn = requireText(value, field).replace("-", "").replace(" ", "").toUpperCase();
        if (!isbn.matches("\\d{13}|\\d{9}[\\dX]")) {
            throw new ValidationException(field + " must be 10 or 13 digits (ISBN-10 may end with X).");
        }
        return isbn;
    }

    /** Semester format: FALL|SPRING|SUMMER followed by a 4-digit year, e.g. FALL 2026. */
    static String validateSemester(String value, String field) {
        String semester = requireText(value, field).toUpperCase().replaceAll("\\s+", " ");
        if (!semester.matches("(" + String.join("|", allowedValues("SEMESTER_TERM")) + ") \\d{4}")) {
            throw new ValidationException(field + " must look like FALL 2026, SPRING 2027 or SUMMER 2027.");
        }
        int year = Integer.parseInt(semester.substring(semester.length() - 4));
        requireRange(year, 2000, 2100, field + " year");
        return semester;
    }

    // ------------------------------------------------------------------ allowed values

    /** Single source of truth for every enum-like status value used in the system. */
    static String[] allowedValues(String category) {
        switch (category) {
            case "ACADEMIC_STATUS":
                return new String[] {"ACTIVE", "INACTIVE"};
            case "EMPLOYMENT_TYPE":
                return new String[] {"FULL_TIME", "PART_TIME", "CONTRACT"};
            case "ENROLLMENT_STATUS":
                return new String[] {"ACTIVE", "COMPLETED", "DROPPED"};
            case "ASSESSMENT_TYPE":
                return new String[] {"QUIZ", "ASSIGNMENT", "MIDTERM", "FINAL_EXAM", "PROJECT"};
            case "PAYMENT_METHOD":
                return new String[] {"CASH", "BANK_TRANSFER", "MOBILE_MONEY", "CARD"};
            case "SEMESTER_TERM":
                return new String[] {"FALL", "SPRING", "SUMMER"};
            default:
                throw new IllegalArgumentException("Unknown value category: " + category);
        }
    }

    static String requireAllowed(String value, String field, String[] allowed) {
        String text = requireText(value, field).toUpperCase();
        for (String option : allowed) {
            if (option.equals(text)) {
                return option;
            }
        }
        throw new ValidationException(field + " must be one of " + String.join(", ", allowed) + ".");
    }

    // ------------------------------------------------------------------ dates

    static LocalDate parseDate(String raw, String field) {
        String text = requireText(raw, field);
        return LocalDate.parse(text, DateTimeFormatter.ofPattern(DATE_PATTERN).withResolverStyle(ResolverStyle.STRICT));
    }

    static String formatDate(LocalDate date) {
        return date == null ? "-" : date.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
    }

    static LocalDate requireDate(LocalDate date, String field) {
        if (date == null) {
            throw new ValidationException(field + " is required.");
        }
        return date;
    }

    /** Strictly before today (used for date of birth). */
    static LocalDate requirePastDate(LocalDate date, String field) {
        requireDate(date, field);
        if (!date.isBefore(LocalDate.now())) {
            throw new ValidationException(field + " must be in the past.");
        }
        if (date.isBefore(LocalDate.of(1900, 1, 1))) {
            throw new ValidationException(field + " cannot be before 1900-01-01.");
        }
        return date;
    }

    /** Today or earlier (admission, hire, enrollment, payment dates...). */
    static LocalDate requireNotFuture(LocalDate date, String field) {
        requireDate(date, field);
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException(field + " cannot be in the future (today is " + formatDate(LocalDate.now()) + ").");
        }
        return date;
    }

    static LocalDate requireNotBefore(LocalDate date, LocalDate earliest, String field, String earliestLabel) {
        requireDate(date, field);
        if (earliest != null && date.isBefore(earliest)) {
            throw new ValidationException(field + " cannot be before the " + earliestLabel + " (" + formatDate(earliest) + ").");
        }
        return date;
    }

    // ------------------------------------------------------------------ numbers and references

    static int requireRange(int value, int min, int max, String field) {
        if (value < min || value > max) {
            throw new ValidationException(field + " must be between " + min + " and " + max + " (you entered " + value + ").");
        }
        return value;
    }

    /** Money: strictly positive, at most two decimal places. Throws NumberFormatException for non-numbers. */
    static BigDecimal parseMoney(String raw, String field) {
        return requirePositive(new BigDecimal(requireText(raw, field).replace(",", "")), field);
    }

    static BigDecimal requirePositive(BigDecimal amount, String field) {
        if (amount == null) {
            throw new ValidationException(field + " is required.");
        }
        if (amount.signum() <= 0) {
            throw new ValidationException(field + " must be greater than zero.");
        }
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new ValidationException(field + " can have at most 2 decimal places.");
        }
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    static <T> T requireReference(T reference, String field) {
        if (reference == null) {
            throw new ValidationException(field + " must reference an existing registered record.");
        }
        return reference;
    }

    // ------------------------------------------------------------------ printing helpers

    static void printHeading(String title) {
        System.out.println("+--------------------------------------------------------------+");
        System.out.println("  " + title);
        System.out.println("+--------------------------------------------------------------+");
    }

    static void printSection(String title) {
        System.out.println("  -- " + title + " --");
    }

    static void printRow(String label, Object value) {
        System.out.printf("  %-22s: %s%n", label, value == null ? "-" : value);
    }

    /** "[ID] display name" for related objects, or a placeholder when absent. */
    static String describe(Identifiable record, String whenMissing) {
        return record == null ? whenMissing : "[" + record.getId() + "] " + record.getDisplayName();
    }
}
