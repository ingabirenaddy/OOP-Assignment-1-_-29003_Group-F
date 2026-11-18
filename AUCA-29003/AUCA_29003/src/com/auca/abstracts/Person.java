package com.auca.abstracts;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import java.time.LocalDate;

/**
 * Root of the people hierarchy. It is abstract because "a person" on its own is
 * never registered at AUCA: every real person is a Student, Teacher or
 * Administrator, and getRoleDescription()/isActive() only make sense for those.
 * The compiler therefore forbids new Person(...).
 */
public abstract class Person implements Identifiable, Registrable {

    private final String personId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;

    /** Protected: only subclasses may call it, through super(...). */
    protected Person(String personId, String firstName, String lastName, String email,
                     String phoneNumber, LocalDate dateOfBirth) {
        this.personId = personId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Person-specific operation. Declared here as an abstract method (the
     * permitted abstract-class extension to the interface rule) so that
     * non-person entities such as Course are not forced to implement it.
     */
    public abstract String getRoleDescription();

    @Override
    public String getId() {
        return personId;
    }

    @Override
    public String getDisplayName() {
        return firstName + " " + lastName;
    }

    @Override
    public void validate() throws ValidationException {
        Identifiable.validateId(personId, "Person ID");
        Registrable.validateName(firstName, "First name");
        Registrable.validateName(lastName, "Last name");
        Registrable.validateEmail(email, "Email");
        Registrable.validatePhone(phoneNumber, "Phone");
        Registrable.requirePastDate(dateOfBirth, "Date of birth");
    }

    @Override
    public void printDetails() {
        Registrable.printHeading(getClass().getSimpleName().toUpperCase() + " " + personId
                + "   (" + Identifiable.referenceOf(this) + ")");
        Registrable.printRow("Role", getRoleDescription());
        Registrable.printSection("Person details");
        Registrable.printRow("Person ID", personId);
        Registrable.printRow("First name", firstName);
        Registrable.printRow("Last name", lastName);
        Registrable.printRow("Email", email);
        Registrable.printRow("Phone", phoneNumber);
        Registrable.printRow("Date of birth", Registrable.formatDate(dateOfBirth));
    }

    @Override
    public String getRegistrationSummary() {
        return String.format("%-13s [%s] %-22s %s | %s", getClass().getSimpleName(), personId,
                getDisplayName(), getRoleDescription(), isActive() ? "ACTIVE" : "INACTIVE");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + personId + ", " + getDisplayName() + ", " + email + "]";
    }

    // ------------------------------------------------------------------ getters / setters

    public String getPersonId() {
        return personId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
