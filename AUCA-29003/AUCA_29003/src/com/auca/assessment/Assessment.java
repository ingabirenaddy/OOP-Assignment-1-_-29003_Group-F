package com.auca.assessment;

import com.auca.enrollment.Enrollment;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import java.time.LocalDate;

/** A graded assessment for one Enrollment. */
public class Assessment implements Identifiable, Registrable {

    private final String assessmentId;
    private Enrollment enrollment;
    private String assessmentType;
    private double maximumScore;
    private double obtainedScore;
    private LocalDate assessmentDate;

    public Assessment(String assessmentId, Enrollment enrollment, String assessmentType,
                      double maximumScore, double obtainedScore, LocalDate assessmentDate) {
        this.assessmentId = assessmentId;
        this.enrollment = enrollment;
        this.assessmentType = assessmentType;
        this.maximumScore = maximumScore;
        this.obtainedScore = obtainedScore;
        this.assessmentDate = assessmentDate;
    }

    @Override
    public String getId() {
        return assessmentId;
    }

    @Override
    public String getDisplayName() {
        return assessmentType + " " + formatScore(obtainedScore) + "/" + formatScore(maximumScore)
                + (enrollment == null || enrollment.getCourse() == null ? "" : " in " + enrollment.getCourse().getCourseCode());
    }

    /** An assessment counts while its enrollment is active. */
    @Override
    public boolean isActive() {
        return enrollment != null && enrollment.isActive();
    }

    @Override
    public void validate() throws ValidationException {
        Identifiable.validateId(assessmentId, "Assessment ID");
        Registrable.requireReference(enrollment, "Enrollment");
        Registrable.requireAllowed(assessmentType, "Assessment type", Registrable.allowedValues("ASSESSMENT_TYPE"));
        if (!(maximumScore > 0) || maximumScore > 1000) {
            throw new ValidationException("Maximum score must be greater than 0 and at most 1000.");
        }
        if (obtainedScore < 0 || obtainedScore > maximumScore) {
            throw new ValidationException("Obtained score must be between 0 and the maximum score ("
                    + formatScore(maximumScore) + ").");
        }
        Registrable.requireNotFuture(assessmentDate, "Assessment date");
        Registrable.requireNotBefore(assessmentDate, enrollment.getEnrollmentDate(), "Assessment date", "enrollment date");
    }

    @Override
    public void printDetails() {
        Registrable.printHeading("ASSESSMENT " + assessmentId + "   (" + Identifiable.referenceOf(this) + ")");
        Registrable.printRow("Assessment ID", assessmentId);
        Registrable.printRow("Enrollment", Registrable.describe(enrollment, "-"));
        Registrable.printRow("Assessment type", assessmentType);
        Registrable.printRow("Maximum score", formatScore(maximumScore));
        Registrable.printRow("Obtained score", formatScore(obtainedScore)
                + String.format("  (%.1f%%)", maximumScore > 0 ? obtainedScore * 100 / maximumScore : 0));
        Registrable.printRow("Assessment date", Registrable.formatDate(assessmentDate));
    }

    @Override
    public String getRegistrationSummary() {
        return String.format("Assessment    [%s] %-28s %s", assessmentId, getDisplayName(),
                enrollment == null || enrollment.getStudent() == null ? "" : "| " + enrollment.getStudent().getDisplayName());
    }

    @Override
    public String toString() {
        return "Assessment[" + assessmentId + ", enrollment=" + (enrollment == null ? "-" : enrollment.getId())
                + ", " + assessmentType + ", " + obtainedScore + "/" + maximumScore + "]";
    }

    private static String formatScore(double score) {
        return score == Math.rint(score) ? String.valueOf((long) score) : String.valueOf(score);
    }

    // ------------------------------------------------------------------ getters / setters

    public String getAssessmentId() {
        return assessmentId;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
    }

    public double getMaximumScore() {
        return maximumScore;
    }

    public void setMaximumScore(double maximumScore) {
        this.maximumScore = maximumScore;
    }

    public double getObtainedScore() {
        return obtainedScore;
    }

    public void setObtainedScore(double obtainedScore) {
        this.obtainedScore = obtainedScore;
    }

    public LocalDate getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(LocalDate assessmentDate) {
        this.assessmentDate = assessmentDate;
    }
}
