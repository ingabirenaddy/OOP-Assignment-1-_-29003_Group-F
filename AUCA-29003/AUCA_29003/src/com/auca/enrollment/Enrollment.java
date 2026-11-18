package com.auca.enrollment;

import com.auca.course.Course;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.student.Student;
import java.time.LocalDate;

/** Links one Student to one Course for one semester. */
public class Enrollment implements Identifiable, Registrable {

    private final String enrollmentId;
    private Student student;
    private Course course;
    private LocalDate enrollmentDate;
    private String semester;
    private String enrollmentStatus;

    public Enrollment(String enrollmentId, Student student, Course course, LocalDate enrollmentDate,
                      String semester, String enrollmentStatus) {
        this.enrollmentId = enrollmentId;
        this.student = student;
        this.course = course;
        this.enrollmentDate = enrollmentDate;
        this.semester = semester;
        this.enrollmentStatus = enrollmentStatus;
    }

    @Override
    public String getId() {
        return enrollmentId;
    }

    @Override
    public String getDisplayName() {
        return (student == null ? "-" : student.getDisplayName()) + " -> "
                + (course == null ? "-" : course.getCourseCode()) + " (" + semester + ", " + enrollmentStatus + ")";
    }

    @Override
    public boolean isActive() {
        return Identifiable.DEFAULT_STATUS.equals(enrollmentStatus);
    }

    @Override
    public void validate() throws ValidationException {
        Identifiable.validateId(enrollmentId, "Enrollment ID");
        Registrable.requireReference(student, "Student");
        Registrable.requireReference(course, "Course");
        Registrable.requireNotFuture(enrollmentDate, "Enrollment date");
        Registrable.validateSemester(semester, "Semester");
        Registrable.requireAllowed(enrollmentStatus, "Enrollment status", Registrable.allowedValues("ENROLLMENT_STATUS"));
    }

    @Override
    public void printDetails() {
        Registrable.printHeading("ENROLLMENT " + enrollmentId + "   (" + Identifiable.referenceOf(this) + ")");
        Registrable.printRow("Enrollment ID", enrollmentId);
        Registrable.printRow("Student", Registrable.describe(student, "-"));
        Registrable.printRow("Course", Registrable.describe(course, "-"));
        Registrable.printRow("Enrollment date", Registrable.formatDate(enrollmentDate));
        Registrable.printRow("Semester", semester);
        Registrable.printRow("Enrollment status", enrollmentStatus);
    }

    @Override
    public String getRegistrationSummary() {
        return String.format("Enrollment    [%s] %s", enrollmentId, getDisplayName());
    }

    @Override
    public String toString() {
        return "Enrollment[" + enrollmentId + ", student=" + (student == null ? "-" : student.getId())
                + ", course=" + (course == null ? "-" : course.getId()) + ", " + semester + ", " + enrollmentStatus + "]";
    }

    // ------------------------------------------------------------------ getters / setters

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void setEnrollmentStatus(String enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }
}
