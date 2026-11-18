package com.auca.teacher;

import com.auca.abstracts.AcademicMember;
import com.auca.department.Department;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Person -> AcademicMember -> Teacher (multilevel inheritance). */
public class Teacher extends AcademicMember {

    private String employeeNumber;
    private String specialization;
    private LocalDate hireDate;
    private BigDecimal salary;
    private String officeNumber;
    private String employmentType;

    public Teacher(String personId, String firstName, String lastName, String email,
                   String phoneNumber, LocalDate dateOfBirth,
                   String memberNumber, Department department, LocalDate joinDate,
                   String academicStatus, String institutionalEmail, String advisorName,
                   String employeeNumber, String specialization, LocalDate hireDate,
                   BigDecimal salary, String officeNumber, String employmentType) {
        super(personId, firstName, lastName, email, phoneNumber, dateOfBirth,
                memberNumber, department, joinDate, academicStatus, institutionalEmail, advisorName);
        this.employeeNumber = employeeNumber;
        this.specialization = specialization;
        this.hireDate = hireDate;
        this.salary = salary;
        this.officeNumber = officeNumber;
        this.employmentType = employmentType;
    }

    @Override
    public String getRoleDescription() {
        return "Teacher specialising in " + specialization;
    }

    @Override
    public String getAcademicCategory() {
        return "Faculty member (" + employmentType + ")";
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        Identifiable.validateId(employeeNumber, "Employee number");
        Registrable.validateName(specialization, "Specialization");
        Registrable.requireNotFuture(hireDate, "Hire date");
        Registrable.requireNotBefore(hireDate, getDateOfBirth(), "Hire date", "date of birth");
        Registrable.requirePositive(salary, "Salary");
        Registrable.validateCode(officeNumber, "Office number", 1, 10);
        Registrable.requireAllowed(employmentType, "Employment type", Registrable.allowedValues("EMPLOYMENT_TYPE"));
    }

    @Override
    public void printDetails() {
        super.printDetails();
        Registrable.printSection("Teacher details");
        Registrable.printRow("Employee number", employeeNumber);
        Registrable.printRow("Specialization", specialization);
        Registrable.printRow("Hire date", Registrable.formatDate(hireDate));
        Registrable.printRow("Salary (RWF)", salary == null ? "-" : salary.toPlainString());
        Registrable.printRow("Office number", officeNumber);
        Registrable.printRow("Employment type", employmentType);
    }

    @Override
    public String toString() {
        return super.toString() + "{employeeNumber=" + employeeNumber + ", specialization=" + specialization
                + ", type=" + employmentType + "}";
    }

    // ------------------------------------------------------------------ getters / setters

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public String getOfficeNumber() {
        return officeNumber;
    }

    public void setOfficeNumber(String officeNumber) {
        this.officeNumber = officeNumber;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }
}
