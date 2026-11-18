package com.auca.student;

import com.auca.abstracts.AcademicMember;
import com.auca.department.Department;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import java.time.LocalDate;

/** Person -> AcademicMember -> Student (multilevel inheritance). */
public class Student extends AcademicMember {

    private String studentNumber;
    private String programName;
    private LocalDate admissionDate;
    private int studyYear;
    private String guardianPhone;
    private boolean scholarshipStatus;

    public Student(String personId, String firstName, String lastName, String email,
                   String phoneNumber, LocalDate dateOfBirth,
                   String memberNumber, Department department, LocalDate joinDate,
                   String academicStatus, String institutionalEmail, String advisorName,
                   String studentNumber, String programName, LocalDate admissionDate,
                   int studyYear, String guardianPhone, boolean scholarshipStatus) {
        super(personId, firstName, lastName, email, phoneNumber, dateOfBirth,
                memberNumber, department, joinDate, academicStatus, institutionalEmail, advisorName);
        this.studentNumber = studentNumber;
        this.programName = programName;
        this.admissionDate = admissionDate;
        this.studyYear = studyYear;
        this.guardianPhone = guardianPhone;
        this.scholarshipStatus = scholarshipStatus;
    }

    @Override
    public String getRoleDescription() {
        return "Student of " + programName + ", year " + studyYear;
    }

    @Override
    public String getAcademicCategory() {
        return (scholarshipStatus ? "Scholarship" : "Self-sponsored") + " student, year " + studyYear;
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        Identifiable.validateId(studentNumber, "Student number");
        Registrable.validateLongText(programName, "Program name");
        Registrable.requireNotFuture(admissionDate, "Admission date");
        Registrable.requireNotBefore(admissionDate, getDateOfBirth(), "Admission date", "date of birth");
        Registrable.requireRange(studyYear, 1, 6, "Study year");
        Registrable.validatePhone(guardianPhone, "Guardian phone");
    }

    @Override
    public void printDetails() {
        super.printDetails();
        Registrable.printSection("Student details");
        Registrable.printRow("Student number", studentNumber);
        Registrable.printRow("Program name", programName);
        Registrable.printRow("Admission date", Registrable.formatDate(admissionDate));
        Registrable.printRow("Study year", studyYear);
        Registrable.printRow("Guardian phone", guardianPhone);
        Registrable.printRow("Scholarship", scholarshipStatus ? "YES" : "NO");
    }

    @Override
    public String getDisplayName() {
        return super.getDisplayName() + " (" + studentNumber + ")";
    }

    @Override
    public String toString() {
        return super.toString() + "{studentNumber=" + studentNumber + ", program=" + programName
                + ", year=" + studyYear + "}";
    }

    // ------------------------------------------------------------------ getters / setters

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public LocalDate getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(LocalDate admissionDate) {
        this.admissionDate = admissionDate;
    }

    public int getStudyYear() {
        return studyYear;
    }

    public void setStudyYear(int studyYear) {
        this.studyYear = studyYear;
    }

    public String getGuardianPhone() {
        return guardianPhone;
    }

    public void setGuardianPhone(String guardianPhone) {
        this.guardianPhone = guardianPhone;
    }

    public boolean isScholarshipStatus() {
        return scholarshipStatus;
    }

    public void setScholarshipStatus(boolean scholarshipStatus) {
        this.scholarshipStatus = scholarshipStatus;
    }
}
