package com.auca.department;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.teacher.Teacher;

/** An academic department. The head teacher is optional at creation and can be assigned later. */
public class Department implements Identifiable, Registrable {

    private final String departmentId;
    private String departmentName;
    private String departmentCode;
    private String officeLocation;
    private String contactEmail;
    private Teacher headTeacher;

    /** Department without a head teacher yet; chains to the full constructor with this(...). */
    public Department(String departmentId, String departmentName, String departmentCode,
                      String officeLocation, String contactEmail) {
        this(departmentId, departmentName, departmentCode, officeLocation, contactEmail, null);
    }

    public Department(String departmentId, String departmentName, String departmentCode,
                      String officeLocation, String contactEmail, Teacher headTeacher) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
        this.officeLocation = officeLocation;
        this.contactEmail = contactEmail;
        this.headTeacher = headTeacher;
    }

    @Override
    public String getId() {
        return departmentId;
    }

    @Override
    public String getDisplayName() {
        return departmentName + " (" + departmentCode + ")";
    }

    /** A department is operational (active) once it has a head teacher. */
    @Override
    public boolean isActive() {
        return headTeacher != null;
    }

    @Override
    public void validate() throws ValidationException {
        Identifiable.validateId(departmentId, "Department ID");
        Registrable.validateName(departmentName, "Department name");
        Registrable.validateCode(departmentCode, "Department code", 2, 10);
        Registrable.validateName(officeLocation, "Office location");
        Registrable.validateEmail(contactEmail, "Contact email");
        // headTeacher is optional at creation, so null is allowed here.
    }

    @Override
    public void printDetails() {
        Registrable.printHeading("DEPARTMENT " + departmentId + "   (" + Identifiable.referenceOf(this) + ")");
        Registrable.printRow("Department ID", departmentId);
        Registrable.printRow("Department name", departmentName);
        Registrable.printRow("Department code", departmentCode);
        Registrable.printRow("Office location", officeLocation);
        Registrable.printRow("Contact email", contactEmail);
        Registrable.printRow("Head teacher", Registrable.describe(headTeacher, "Not assigned"));
    }

    @Override
    public String getRegistrationSummary() {
        return String.format("Department    [%s] %-22s Office: %s | Head: %s", departmentId, getDisplayName(),
                officeLocation, Registrable.describe(headTeacher, "Not assigned"));
    }

    @Override
    public String toString() {
        // Only the head teacher's ID is printed to avoid endless Department <-> Teacher recursion.
        return "Department[" + departmentId + ", " + departmentName + ", code=" + departmentCode
                + ", head=" + (headTeacher == null ? "none" : headTeacher.getId()) + "]";
    }

    // ------------------------------------------------------------------ getters / setters

    public String getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public Teacher getHeadTeacher() {
        return headTeacher;
    }

    public void setHeadTeacher(Teacher headTeacher) {
        this.headTeacher = headTeacher;
    }
}
