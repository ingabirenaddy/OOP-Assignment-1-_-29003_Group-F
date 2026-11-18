package com.auca.bookloan;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.librarybook.LibraryBook;
import com.auca.student.Student;
import java.time.LocalDate;

/** One copy of a LibraryBook lent to a Student. returnedDate stays null until the book is returned. */
public class BookLoan implements Identifiable, Registrable {

    private final String loanId;
    private Student student;
    private LibraryBook book;
    private LocalDate borrowedDate;
    private LocalDate dueDate;
    private LocalDate returnedDate;

    /** New loan (not yet returned); chains to the full constructor with this(...). */
    public BookLoan(String loanId, Student student, LibraryBook book, LocalDate borrowedDate, LocalDate dueDate) {
        this(loanId, student, book, borrowedDate, dueDate, null);
    }

    public BookLoan(String loanId, Student student, LibraryBook book, LocalDate borrowedDate,
                    LocalDate dueDate, LocalDate returnedDate) {
        this.loanId = loanId;
        this.student = student;
        this.book = book;
        this.borrowedDate = borrowedDate;
        this.dueDate = dueDate;
        this.returnedDate = returnedDate;
    }

    @Override
    public String getId() {
        return loanId;
    }

    @Override
    public String getDisplayName() {
        return (book == null ? "-" : book.getTitle()) + " -> " + (student == null ? "-" : student.getDisplayName())
                + " (due " + Registrable.formatDate(dueDate) + ")";
    }

    /** A loan is active until the book has been returned. */
    @Override
    public boolean isActive() {
        return returnedDate == null;
    }

    @Override
    public void validate() throws ValidationException {
        Identifiable.validateId(loanId, "Loan ID");
        Registrable.requireReference(student, "Student");
        Registrable.requireReference(book, "Book");
        Registrable.requireNotFuture(borrowedDate, "Borrowed date");
        Registrable.requireDate(dueDate, "Due date");
        if (!dueDate.isAfter(borrowedDate)) {
            throw new ValidationException("Due date must be after the borrowed date ("
                    + Registrable.formatDate(borrowedDate) + ").");
        }
        if (returnedDate != null) {
            Registrable.requireNotFuture(returnedDate, "Returned date");
            Registrable.requireNotBefore(returnedDate, borrowedDate, "Returned date", "borrowed date");
        }
    }

    @Override
    public void printDetails() {
        Registrable.printHeading("BOOK LOAN " + loanId + "   (" + Identifiable.referenceOf(this) + ")");
        Registrable.printRow("Loan ID", loanId);
        Registrable.printRow("Student", Registrable.describe(student, "-"));
        Registrable.printRow("Book", book == null ? "-" : "[" + book.getId() + "] " + book.getTitle());
        Registrable.printRow("Borrowed date", Registrable.formatDate(borrowedDate));
        Registrable.printRow("Due date", Registrable.formatDate(dueDate));
        Registrable.printRow("Returned date", returnedDate == null ? "Not returned yet" : Registrable.formatDate(returnedDate));
    }

    @Override
    public String getRegistrationSummary() {
        return String.format("BookLoan      [%s] %-40s %s", loanId, getDisplayName(),
                returnedDate == null ? "ON LOAN" : "RETURNED " + Registrable.formatDate(returnedDate));
    }

    @Override
    public String toString() {
        return "BookLoan[" + loanId + ", student=" + (student == null ? "-" : student.getId())
                + ", book=" + (book == null ? "-" : book.getId()) + ", borrowed=" + borrowedDate
                + ", due=" + dueDate + ", returned=" + returnedDate + "]";
    }

    // ------------------------------------------------------------------ getters / setters

    public String getLoanId() {
        return loanId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public LibraryBook getBook() {
        return book;
    }

    public void setBook(LibraryBook book) {
        this.book = book;
    }

    public LocalDate getBorrowedDate() {
        return borrowedDate;
    }

    public void setBorrowedDate(LocalDate borrowedDate) {
        this.borrowedDate = borrowedDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnedDate() {
        return returnedDate;
    }

    public void setReturnedDate(LocalDate returnedDate) {
        this.returnedDate = returnedDate;
    }
}
