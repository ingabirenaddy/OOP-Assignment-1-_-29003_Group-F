# AUCA_ID – Demonstration & Defence Guide

The examiner will ask you to explain your code. Practise this guide until you can say each answer in your own words.

---

## 1. Design in one minute

* **Interfaces** (`com.auca.interfaces`) hold the six `public static final` constants each, plus the required methods:
  * `Identifiable`: `getId()`, `getDisplayName()` and `isActive()`.
  * `Registrable`: `validate() throws ValidationException`, `printDetails()` and `getRegistrationSummary()`.
* `Registrable` also has **static validation helpers**, such as `validateName`, `validateEmail`, `validatePhone`, `parseDate`, `parseMoney`, `requireRange` and `allowedValues`.
  The domain classes call them inside `validate()`, and the console forms call them for each field. Each rule is written once.
* **Abstract classes**:
  * `Person` implements both interfaces and declares `public abstract String getRoleDescription()`.
  * `AcademicMember extends Person` and declares `public abstract String getAcademicCategory()`.
* **Inheritance**:
  * Multilevel: `Person → AcademicMember → Student`, and `Person → AcademicMember → Teacher`.
  * Hierarchical: `Person` has two direct children, `AcademicMember` and `Administrator`.
* **The other 8 concrete classes** implement `Identifiable` and `Registrable` directly.
* **Relationships use object references**, not text IDs. For example, `Course.department` is a `Department` object, `Enrollment.student` is a `Student` and `BookLoan.book` is a `LibraryBook`.
* **`Main`** holds the typed `ArrayList`s, the 19-option menu (an enhanced `switch`), one shared `Scanner`, and the smart-form engine.

## 2. Answers to likely questions

**Why can't `Person` and `AcademicMember` be instantiated?**
They are `abstract`. They also contain abstract methods (`getRoleDescription()` and `getAcademicCategory()`) that have no meaning until you know whether the person is a Student, Teacher or Administrator. `new Person(...)` is a compile error. Their constructors are `protected`, so only subclasses can call them through `super(...)`.

**Where did you put `getRoleDescription()` and `getAcademicCategory()`, and why?**
I used the "cleaner alternative" from Section 4. They are abstract methods in `Person` and `AcademicMember`, not methods in `Registrable`. Putting them in `Registrable` would force Course, LibraryBook and the other non-person classes to implement person-specific methods that make no sense for them. The brief allows this as the permitted abstract-class extension to the interface rule.

**Show `super(...)` and constructor chaining.**
* `Student` calls `super(...)` with 12 arguments, which goes to `AcademicMember`.
* `AcademicMember` calls `super(...)` with 6 arguments, which goes to `Person`.
* `Department` and `BookLoan` also have a short constructor that chains to the full one with `this(..., null)`.

**Show method overriding.**
* `printDetails()` is overridden at every level, and each level calls `super.printDetails()` first and then adds its own section. You can see this in option 19, part 3.
* `validate()` works the same way: `Student.validate()` calls `super.validate()`, which checks the AcademicMember fields and then the Person fields.
* `isActive()`, `getDisplayName()` and `toString()` are also overridden.

**Where is runtime polymorphism?**
Use menu option 19. It shows three things:
* `List<Person>` holds Students, Teachers and Administrators. Calling `p.getRoleDescription()` on each one runs the subclass's version, chosen at runtime.
* `List<Identifiable>` holds unrelated classes (Department, Course, BookLoan and so on), and they are used through one interface.
* A `Registrable` reference calls `printDetails()`.

**Why are some exceptions checked and some unchecked?**
* `ValidationException` and `DuplicateRecordException` extend `RuntimeException`. They are raised during data entry, inside setters/validators and inside the lambda field parsers (`Function<String,Object>`). A lambda like that cannot throw a checked exception.
* `RecordNotFoundException`, `PrerequisiteMissingException` and `BookUnavailableException` extend `Exception`, so they are checked. They are workflow failures, and the compiler forces the caller to handle them (search, prerequisite check, borrow).

**Where are exceptions caught?**
* `askField` catches `ValidationException`, `DuplicateRecordException`, `DateTimeParseException` and `NumberFormatException`. It prints the reason and asks again for only that field.
* `fillForm` and `editRecord` catch rule failures at save time and send you back to the review screen.
* The main loop has a last-resort `catch (RuntimeException e)`, so one failed operation never closes the program.
* No catch block is empty.

**Why `BigDecimal` and `LocalDate`?**
* `double` cannot represent money exactly: `0.1 + 0.2 != 0.3`. `BigDecimal` is exact. Amounts must be greater than 0 and have at most 2 decimal places.
* `LocalDate.parse` uses a STRICT formatter (`DATE_PATTERN = "uuuu-MM-dd"`), which rejects dates such as 2025-02-30.

**Why `nextLine()` only?**
`nextInt()` leaves the newline in the buffer, so the next `nextLine()` returns an empty string and skips an input. Every input in the program goes through one `readLine()` method that uses the shared `Scanner`. The program then converts and validates the text explicitly.

**How does a missing prerequisite work?**
* `ensurePrerequisite(...)` throws and catches `PrerequisiteMissingException`. It shows a message such as "No Course exists. Register a course before enrolling a student." and then offers *1 Register Course now / 2 Return to menu*.
* Inside a form, the selection list has a `[+] Register a new …` option, which opens a **nested** form.
* The outer form's values stay in its `Object[]` array, so the outer form resumes at the same field when the nested form finishes.
* No placeholder records are ever created.

**How is book stock kept correct?**
* `lendCopy()` decrements `availableCopies` **once**, and only after the loan passed every validation. If no copy is left, it throws `BookUnavailableException`.
* `receiveCopy()` sets `returnedDate` on the **existing** loan and increments stock once. If validation fails, it rolls back.
* Cancelling a form never touches stock.

**How does Update work?**
* Option 15 finds the record by ID (or throws `RecordNotFoundException`) and opens the same form in review mode.
* The primary ID is locked. Uniqueness checks ignore the record being edited (`editingRecord`).
* You see `old -> new` for each change and must confirm. If validation fails, the original values are restored.

## 3. Demo script (≈ 10 minutes)

Use today's date or an earlier one for all dates, because future dates are rejected. Use dates after the date of birth.

1. **2 Register Student**, with no department registered yet.
   * Step 1: type a bad date such as `2004-02-30`. It is rejected and only field 06 is asked again.
   * Step 2, field 08: there is no department, so choose `1 [+] Register a new Department`. Fill in the department, choose `Leave unassigned` for head teacher, then save. The program returns to field 08 and the department is selected.
   * Step 3: type `7` for study year. It is rejected (the range is 1–6).
   * Review: `2` Edit field → `02` and change the first name, then `1` Save.
2. **Duplicate check:** choose `[1] Register another` and reuse the same email or student number. Both are rejected immediately.
3. **7 Enroll Student** with no course. The message "No Course exists…" appears; choose `1`.
   * The Course form needs a Teacher, so register one. Type `abc` and then `-5` for salary; both are rejected, then type a real salary.
   * Accept "assign as head of department?".
   * The Course is saved, and the program returns to the Enrollment.
   * Save the enrollment, then try the same student, course and semester as ACTIVE again. You get a duplicate error.
4. **8 Record Assessment**: enter max `0` (rejected), then obtained greater than max (rejected), then an assessment date before the enrollment date (rejected).
5. **9 Record Fee Payment**: enter amount `12.345` (rejected), then reuse a receipt number (rejected).
6. **11 Borrow Library Book**: there is no book yet, so register one inline with 1 copy. Then borrow it.
   * Enter a due date before the borrowed date (rejected).
   * After saving, the stock shows 0/1. Run **11** again and the program says no copies are available.
7. **12 Return Library Book**: enter a date before the borrowed date (rejected), then a valid date and confirm. Stock goes back to 1/1, and an overdue note is shown if the return is late.
8. **13 View All**, **14 Search** (try an unknown ID first), **15 Update** (change the email and confirm), **16 Summary**, **17 Payment history** and **18 Available books**.
9. **6 Register Classroom**: type `CANCEL` part-way and confirm. Then show in 13 that nothing was saved.
10. **19 Demonstrate Polymorphism**.
11. At the main menu, type `abc` and then `25`. The program says the input is invalid and does not crash.

## 4. Business rules implemented

| Rule | Where |
|---|---|
| IDs 4–30 characters, unique across all records | `Identifiable.validateId`, `Main.uniqueId` |
| Names 2–60 characters (long texts 2–120) | `Registrable.validateName` / `validateLongText` |
| Email syntax, max 100 characters; person email and institutional email unique | `Registrable.validateEmail` |
| Phone 10–15 digits, optional leading `+` | `Registrable.validatePhone` |
| Date of birth in the past; admission/hire/enrollment/payment dates not in the future | `requirePastDate` / `requireNotFuture` |
| Study year 1–6; credit hours 1–6; capacity > 0; floor ≥ 0; access level 1–5 | `requireRange` |
| Salary and payments strictly positive (`BigDecimal`, 2 decimal places) | `parseMoney` / `requirePositive` |
| `0 ≤ availableCopies ≤ totalCopies`; on update, copies on loan are respected | `LibraryBook.validate`, `Main.updateBook` |
| Maximum score > 0; obtained score between 0 and the maximum; only ACTIVE enrollments can be assessed | `Assessment.validate`, `Main.buildAssessment` |
| Due date after borrowed date; returned date empty until return, never before borrowed date | `BookLoan.validate` |
| No duplicate ACTIVE enrollment for the same student, course and semester | `Main.checkEnrollmentRules` |
| Unique student/employee/staff/member numbers, course and department codes, receipts, ISBN, building+room | readers + builders in `Main` |
| Allowed values: academic status, employment type, enrollment status, assessment type, payment method, semester | `Registrable.allowedValues` |
