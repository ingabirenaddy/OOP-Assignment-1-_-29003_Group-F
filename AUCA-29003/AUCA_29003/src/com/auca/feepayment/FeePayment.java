package com.auca.feepayment;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.student.Student;
import java.math.BigDecimal;
import java.time.LocalDate;

/** A fee payment made by a Student. Money is stored as BigDecimal, never double. */
public class FeePayment implements Identifiable, Registrable {

    private final String paymentId;
    private Student student;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String receiptNumber;

    public FeePayment(String paymentId, Student student, BigDecimal amount, LocalDate paymentDate,
                      String paymentMethod, String receiptNumber) {
        this.paymentId = paymentId;
        this.student = student;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.receiptNumber = receiptNumber;
    }

    @Override
    public String getId() {
        return paymentId;
    }

    @Override
    public String getDisplayName() {
        return "Receipt " + receiptNumber + " - RWF " + (amount == null ? "-" : amount.toPlainString());
    }

    /** A recorded payment is always valid/active once saved. */
    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void validate() throws ValidationException {
        Identifiable.validateId(paymentId, "Payment ID");
        Registrable.requireReference(student, "Student");
        Registrable.requirePositive(amount, "Amount");
        Registrable.requireNotFuture(paymentDate, "Payment date");
        Registrable.requireAllowed(paymentMethod, "Payment method", Registrable.allowedValues("PAYMENT_METHOD"));
        Identifiable.validateId(receiptNumber, "Receipt number");
    }

    @Override
    public void printDetails() {
        Registrable.printHeading("FEE PAYMENT " + paymentId + "   (" + Identifiable.referenceOf(this) + ")");
        Registrable.printRow("Payment ID", paymentId);
        Registrable.printRow("Student", Registrable.describe(student, "-"));
        Registrable.printRow("Amount (RWF)", amount == null ? "-" : amount.toPlainString());
        Registrable.printRow("Payment date", Registrable.formatDate(paymentDate));
        Registrable.printRow("Payment method", paymentMethod);
        Registrable.printRow("Receipt number", receiptNumber);
    }

    @Override
    public String getRegistrationSummary() {
        return String.format("FeePayment    [%s] %-30s %s | %s | %s", paymentId, getDisplayName(),
                Registrable.formatDate(paymentDate), paymentMethod, student == null ? "-" : student.getDisplayName());
    }

    @Override
    public String toString() {
        return "FeePayment[" + paymentId + ", student=" + (student == null ? "-" : student.getId())
                + ", amount=" + amount + ", " + paymentMethod + ", receipt=" + receiptNumber + "]";
    }

    // ------------------------------------------------------------------ getters / setters

    public String getPaymentId() {
        return paymentId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }
}
