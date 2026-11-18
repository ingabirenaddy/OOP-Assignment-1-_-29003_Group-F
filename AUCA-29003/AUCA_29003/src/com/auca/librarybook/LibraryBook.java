package com.auca.librarybook;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;

/** A library title with its copy stock. availableCopies is changed only by borrowing and returning. */
public class LibraryBook implements Identifiable, Registrable {

    private final String bookId;
    private String isbn;
    private String title;
    private String author;
    private int totalCopies;
    private int availableCopies;

    public LibraryBook(String bookId, String isbn, String title, String author,
                       int totalCopies, int availableCopies) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    @Override
    public String getId() {
        return bookId;
    }

    @Override
    public String getDisplayName() {
        return title + " by " + author + " (" + availableCopies + "/" + totalCopies + " available)";
    }

    /** A book is active (borrowable) when at least one copy is on the shelf. */
    @Override
    public boolean isActive() {
        return availableCopies > 0;
    }

    @Override
    public void validate() throws ValidationException {
        Identifiable.validateId(bookId, "Book ID");
        Registrable.validateIsbn(isbn, "ISBN");
        Registrable.validateLongText(title, "Title");
        Registrable.validateName(author, "Author");
        Registrable.requireRange(totalCopies, 0, 10000, "Total copies");
        Registrable.requireRange(availableCopies, 0, totalCopies, "Available copies");
    }

    @Override
    public void printDetails() {
        Registrable.printHeading("LIBRARY BOOK " + bookId + "   (" + Identifiable.referenceOf(this) + ")");
        Registrable.printRow("Book ID", bookId);
        Registrable.printRow("ISBN", isbn);
        Registrable.printRow("Title", title);
        Registrable.printRow("Author", author);
        Registrable.printRow("Total copies", totalCopies);
        Registrable.printRow("Available copies", availableCopies);
    }

    @Override
    public String getRegistrationSummary() {
        return String.format("LibraryBook   [%s] %-30s by %s | ISBN %s | %d/%d available", bookId, title, author,
                isbn, availableCopies, totalCopies);
    }

    @Override
    public String toString() {
        return "LibraryBook[" + bookId + ", " + title + ", " + author + ", isbn=" + isbn
                + ", " + availableCopies + "/" + totalCopies + "]";
    }

    // ------------------------------------------------------------------ getters / setters

    public String getBookId() {
        return bookId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }
}
