package com.auca.abstracts;

import com.auca.department.Department;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import java.time.LocalDate;

/**
 * Middle level of the multilevel hierarchy Person -> AcademicMember -> Student/Teacher.
 * Abstract because an academic member is always either a Student or a Teacher;
 * getAcademicCategory() and getRoleDescription() have no meaning until then.
 */
public abstract class AcademicMember extends Person {

    private String memberNumber;
    private Department department;
    private LocalDate joinDate;
    private String academicStatus;
    private String institutionalEmail;
    private String advisorName;

    protected AcademicMember(String personId, String firstName, String lastName, String email,
                             String phoneNumber, LocalDate dateOfBirth,
                             String memberNumber, Department department, LocalDate joinDate,
                             String academicStatus, String institutionalEmail, String advisorName) {
        super(personId, firstName, lastName, email, phoneNumber, dateOfBirth); // constructor chaining
        this.memberNumber = memberNumber;
        this.department = department;
        this.joinDate = joinDate;
        this.academicStatus = academicStatus;
        this.institutionalEmail = institutionalEmail;
        this.advisorName = advisorName;
    }

    /** Academic-member-specific operation (permitted abstract-class extension). */
    public abstract String getAcademicCategory();

    @Override
    public boolean isActive() {
        return Identifiable.DEFAULT_STATUS.equals(academicStatus);
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        Identifiable.validateId(memberNumber, "Member number");
        Registrable.requireReference(department, "Department");
        Registrable.requireNotFuture(joinDate, "Join date");
        Registrable.requireNotBefore(joinDate, getDateOfBirth(), "Join date", "date of birth");
        Registrable.requireAllowed(academicStatus, "Academic status", Registrable.allowedValues("ACADEMIC_STATUS"));
        Registrable.validateEmail(institutionalEmail, "Institutional email");
        Registrable.validateLongText(advisorName, "Advisor name");
    }

    @Override
    public void printDetails() {
        super.printDetails();
        Registrable.printSection("Academic member details");
        Registrable.printRow("Category", getAcademicCategory());
        Registrable.printRow("Member number", memberNumber);
        Registrable.printRow("Department", Registrable.describe(department, "-"));
        Registrable.printRow("Join date", Registrable.formatDate(joinDate));
        Registrable.printRow("Academic status", academicStatus);
        Registrable.printRow("Institutional email", institutionalEmail);
        Registrable.printRow("Advisor name", advisorName);
    }

    @Override
    public String toString() {
        return super.toString() + "{member=" + memberNumber + ", department="
                + (department == null ? "-" : department.getId()) + ", status=" + academicStatus + "}";
    }

    // ------------------------------------------------------------------ getters / setters

    public String getMemberNumber() {
        return memberNumber;
    }

    public void setMemberNumber(String memberNumber) {
        this.memberNumber = memberNumber;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public String getAcademicStatus() {
        return academicStatus;
    }

    public void setAcademicStatus(String academicStatus) {
        this.academicStatus = academicStatus;
    }

    public String getInstitutionalEmail() {
        return institutionalEmail;
    }

    public void setInstitutionalEmail(String institutionalEmail) {
        this.institutionalEmail = institutionalEmail;
    }

    public String getAdvisorName() {
        return advisorName;
    }

    public void setAdvisorName(String advisorName) {
        this.advisorName = advisorName;
    }
}
