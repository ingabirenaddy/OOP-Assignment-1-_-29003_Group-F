package com.auca.administrator;

import com.auca.abstracts.Person;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import java.time.LocalDate;

/** Person -> Administrator. Together with AcademicMember this shows hierarchical inheritance. */
public class Administrator extends Person {

    private String staffNumber;
    private String jobTitle;
    private String assignedOffice;
    private LocalDate employmentDate;
    private int accessLevel;
    private boolean active;

    public Administrator(String personId, String firstName, String lastName, String email,
                         String phoneNumber, LocalDate dateOfBirth,
                         String staffNumber, String jobTitle, String assignedOffice,
                         LocalDate employmentDate, int accessLevel, boolean active) {
        super(personId, firstName, lastName, email, phoneNumber, dateOfBirth);
        this.staffNumber = staffNumber;
        this.jobTitle = jobTitle;
        this.assignedOffice = assignedOffice;
        this.employmentDate = employmentDate;
        this.accessLevel = accessLevel;
        this.active = active;
    }

    @Override
    public String getRoleDescription() {
        return jobTitle + " (access level " + accessLevel + ")";
    }

    /** Implements Identifiable.isActive() and is also the getter of the 'active' attribute. */
    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        Identifiable.validateId(staffNumber, "Staff number");
        Registrable.validateName(jobTitle, "Job title");
        Registrable.validateName(assignedOffice, "Assigned office");
        Registrable.requireNotFuture(employmentDate, "Employment date");
        Registrable.requireNotBefore(employmentDate, getDateOfBirth(), "Employment date", "date of birth");
        Registrable.requireRange(accessLevel, 1, 5, "Access level");
    }

    @Override
    public void printDetails() {
        super.printDetails();
        Registrable.printSection("Administrator details");
        Registrable.printRow("Staff number", staffNumber);
        Registrable.printRow("Job title", jobTitle);
        Registrable.printRow("Assigned office", assignedOffice);
        Registrable.printRow("Employment date", Registrable.formatDate(employmentDate));
        Registrable.printRow("Access level", accessLevel);
        Registrable.printRow("Active", active ? "YES" : "NO");
    }

    @Override
    public String toString() {
        return super.toString() + "{staffNumber=" + staffNumber + ", jobTitle=" + jobTitle
                + ", accessLevel=" + accessLevel + ", active=" + active + "}";
    }

    // ------------------------------------------------------------------ getters / setters

    public String getStaffNumber() {
        return staffNumber;
    }

    public void setStaffNumber(String staffNumber) {
        this.staffNumber = staffNumber;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getAssignedOffice() {
        return assignedOffice;
    }

    public void setAssignedOffice(String assignedOffice) {
        this.assignedOffice = assignedOffice;
    }

    public LocalDate getEmploymentDate() {
        return employmentDate;
    }

    public void setEmploymentDate(LocalDate employmentDate) {
        this.employmentDate = employmentDate;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
