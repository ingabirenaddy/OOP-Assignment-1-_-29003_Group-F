package com.auca.course;

import com.auca.department.Department;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.teacher.Teacher;

/** A course offered by a Department and taught by a Teacher (object references, not text IDs). */
public class Course implements Identifiable, Registrable {

    private final String courseId;
    private String courseCode;
    private String courseTitle;
    private int creditHours;
    private Department department;
    private Teacher assignedTeacher;

    public Course(String courseId, String courseCode, String courseTitle, int creditHours,
                  Department department, Teacher assignedTeacher) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.creditHours = creditHours;
        this.department = department;
        this.assignedTeacher = assignedTeacher;
    }

    @Override
    public String getId() {
        return courseId;
    }

    @Override
    public String getDisplayName() {
        return courseCode + " - " + courseTitle;
    }

    /** A course can run only when it has a teacher. */
    @Override
    public boolean isActive() {
        return assignedTeacher != null;
    }

    @Override
    public void validate() throws ValidationException {
        Identifiable.validateId(courseId, "Course ID");
        Registrable.validateCode(courseCode, "Course code", 3, 12);
        Registrable.validateLongText(courseTitle, "Course title");
        Registrable.requireRange(creditHours, 1, 6, "Credit hours");
        Registrable.requireReference(department, "Department");
        Registrable.requireReference(assignedTeacher, "Assigned teacher");
    }

    @Override
    public void printDetails() {
        Registrable.printHeading("COURSE " + courseId + "   (" + Identifiable.referenceOf(this) + ")");
        Registrable.printRow("Course ID", courseId);
        Registrable.printRow("Course code", courseCode);
        Registrable.printRow("Course title", courseTitle);
        Registrable.printRow("Credit hours", creditHours);
        Registrable.printRow("Department", Registrable.describe(department, "-"));
        Registrable.printRow("Assigned teacher", Registrable.describe(assignedTeacher, "-"));
    }

    @Override
    public String getRegistrationSummary() {
        return String.format("Course        [%s] %-30s %d cr | %s | Teacher: %s", courseId, getDisplayName(),
                creditHours, department == null ? "-" : department.getDepartmentCode(),
                assignedTeacher == null ? "-" : assignedTeacher.getDisplayName());
    }

    @Override
    public String toString() {
        return "Course[" + courseId + ", " + courseCode + ", " + courseTitle + ", credits=" + creditHours
                + ", dept=" + (department == null ? "-" : department.getId())
                + ", teacher=" + (assignedTeacher == null ? "-" : assignedTeacher.getId()) + "]";
    }

    // ------------------------------------------------------------------ getters / setters

    public String getCourseId() {
        return courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public int getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(int creditHours) {
        this.creditHours = creditHours;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Teacher getAssignedTeacher() {
        return assignedTeacher;
    }

    public void setAssignedTeacher(Teacher assignedTeacher) {
        this.assignedTeacher = assignedTeacher;
    }
}
