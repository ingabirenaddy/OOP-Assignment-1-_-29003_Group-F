package com.auca.main;

import com.auca.abstracts.AcademicMember;
import com.auca.abstracts.Person;
import com.auca.administrator.Administrator;
import com.auca.assessment.Assessment;
import com.auca.bookloan.BookLoan;
import com.auca.classroom.Classroom;
import com.auca.course.Course;
import com.auca.department.Department;
import com.auca.enrollment.Enrollment;
import com.auca.exceptions.BookUnavailableException;
import com.auca.exceptions.DuplicateRecordException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.RecordNotFoundException;
import com.auca.exceptions.ValidationException;
import com.auca.feepayment.FeePayment;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.librarybook.LibraryBook;
import com.auca.student.Student;
import com.auca.teacher.Teacher;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;


public class Main {

    // ------------------------------------------------------------------ single shared Scanner
    private static final Scanner SCANNER = new Scanner(System.in);

    // Sentinel values returned by field readers.
    private static final Object BACK = new Object();
    private static final Object CANCEL = new Object();
    private static final Object NONE = new Object();   // optional relation left empty

    private static final int BOX_WIDTH = 62;

    // ------------------------------------------------------------------ typed in-memory storage
    private static final List<Department> departments = new ArrayList<>();
    private static final List<Student> students = new ArrayList<>();
    private static final List<Teacher> teachers = new ArrayList<>();
    private static final List<Administrator> administrators = new ArrayList<>();
    private static final List<Course> courses = new ArrayList<>();
    private static final List<Classroom> classrooms = new ArrayList<>();
    private static final List<Enrollment> enrollments = new ArrayList<>();
    private static final List<Assessment> assessments = new ArrayList<>();
    private static final List<FeePayment> payments = new ArrayList<>();
    private static final List<LibraryBook> books = new ArrayList<>();
    private static final List<BookLoan> loans = new ArrayList<>();

    /** Record currently being updated; ignored by uniqueness checks so it does not clash with itself. */
    private static Object editingRecord;

    // ------------------------------------------------------------------ field hints
    private static final String ID_HINT = "String | unique | " + Identifiable.MIN_ID_LENGTH + "-"
            + Identifiable.MAX_ID_LENGTH + " chars";
    private static final String NAME_HINT = "String | " + Registrable.MIN_NAME_LENGTH + "-"
            + Registrable.MAX_NAME_LENGTH + " chars";
    private static final String LONG_HINT = "String | " + Registrable.MIN_NAME_LENGTH + "-"
            + (Registrable.MAX_NAME_LENGTH * 2) + " chars";
    private static final String DATE_HINT = "LocalDate | YYYY-MM-DD";
    private static final String PHONE_HINT = "String | " + Registrable.MIN_PHONE_LENGTH + "-"
            + Registrable.MAX_PHONE_LENGTH + " digits";
    private static final String YES_NO_HINT = "1 YES / 2 NO";

    private static final String[] ACADEMIC_STATUS = Registrable.allowedValues("ACADEMIC_STATUS");
    private static final String[] EMPLOYMENT_TYPE = Registrable.allowedValues("EMPLOYMENT_TYPE");
    private static final String[] ENROLLMENT_STATUS = Registrable.allowedValues("ENROLLMENT_STATUS");
    private static final String[] ASSESSMENT_TYPE = Registrable.allowedValues("ASSESSMENT_TYPE");
    private static final String[] PAYMENT_METHOD = Registrable.allowedValues("PAYMENT_METHOD");

    // ------------------------------------------------------------------ form definitions
    private static final String[] PERSON_LABELS = {"Person ID", "First name", "Last name", "Email", "Phone", "Date of birth"};
    private static final String[] PERSON_HINTS = {ID_HINT, NAME_HINT, NAME_HINT, "String | unique | email", PHONE_HINT, DATE_HINT};
    private static final String[] ACADEMIC_LABELS = {"Member number", "Department", "Join date", "Academic status",
            "Institutional email", "Advisor name"};
    private static final String[] ACADEMIC_HINTS = {ID_HINT, "Department | select", DATE_HINT, choiceHint(ACADEMIC_STATUS),
            "String | unique | email", LONG_HINT};

    private static final String[] STUDENT_STEPS = {"Personal details", "Academic details", "Student details"};
    private static final String[] STUDENT_LABELS = concat(PERSON_LABELS, ACADEMIC_LABELS,
            new String[] {"Student number", "Program name", "Admission date", "Study year", "Guardian phone", "Scholarship"});
    private static final String[] STUDENT_HINTS = concat(PERSON_HINTS, ACADEMIC_HINTS,
            new String[] {ID_HINT, LONG_HINT, DATE_HINT, "int | 1-6", PHONE_HINT, YES_NO_HINT});
    private static final int[] STUDENT_FIELD_STEP = stepMap(6, 6, 6);

    private static final String[] TEACHER_STEPS = {"Personal details", "Academic details", "Teacher details"};
    private static final String[] TEACHER_LABELS = concat(PERSON_LABELS, ACADEMIC_LABELS,
            new String[] {"Employee number", "Specialization", "Hire date", "Salary", "Office number", "Employment type"});
    private static final String[] TEACHER_HINTS = concat(PERSON_HINTS, ACADEMIC_HINTS,
            new String[] {ID_HINT, NAME_HINT, DATE_HINT, "BigDecimal | > 0 RWF", "String | 1-10 chars",
                    choiceHint(EMPLOYMENT_TYPE)});
    private static final int[] TEACHER_FIELD_STEP = stepMap(6, 6, 6);

    private static final String[] ADMIN_STEPS = {"Personal details", "Employment details", "Access & status"};
    private static final String[] ADMIN_LABELS = concat(PERSON_LABELS,
            new String[] {"Staff number", "Job title", "Assigned office", "Employment date", "Access level", "Active"});
    private static final String[] ADMIN_HINTS = concat(PERSON_HINTS,
            new String[] {ID_HINT, NAME_HINT, NAME_HINT, DATE_HINT, "int | 1-5", YES_NO_HINT});
    private static final int[] ADMIN_FIELD_STEP = stepMap(6, 4, 2);

    private static final String[] DEPARTMENT_STEPS = {"Identification", "Contact details", "Leadership"};
    private static final String[] DEPARTMENT_LABELS = {"Department ID", "Department name", "Department code",
            "Office location", "Contact email", "Head teacher"};
    private static final String[] DEPARTMENT_HINTS = {ID_HINT, "String | unique | 2-60 chars",
            "String | unique | 2-10 letters/digits", NAME_HINT, "String | email", "Teacher | optional select"};
    private static final int[] DEPARTMENT_FIELD_STEP = stepMap(3, 2, 1);

    private static final String[] COURSE_STEPS = {"Course identity", "Department", "Teaching"};
    private static final String[] COURSE_LABELS = {"Course ID", "Course code", "Course title", "Credit hours",
            "Department", "Assigned teacher"};
    private static final String[] COURSE_HINTS = {ID_HINT, "String | unique | 3-12 letters/digits", LONG_HINT,
            "int | 1-6", "Department | select", "Teacher | select"};
    private static final int[] COURSE_FIELD_STEP = stepMap(4, 1, 1);

    private static final String[] CLASSROOM_STEPS = {"Location", "Size & floor", "Availability"};
    private static final String[] CLASSROOM_LABELS = {"Classroom ID", "Building name", "Room number", "Capacity",
            "Floor number", "Available"};
    private static final String[] CLASSROOM_HINTS = {ID_HINT, NAME_HINT, "String | 1-10 chars", "int | 1-1000",
            "int | 0-200", YES_NO_HINT};
    private static final int[] CLASSROOM_FIELD_STEP = stepMap(3, 2, 1);

    private static final String[] ENROLLMENT_STEPS = {"Identification", "Student & course", "Term & status"};
    private static final String[] ENROLLMENT_LABELS = {"Enrollment ID", "Student", "Course", "Enrollment date",
            "Semester", "Enrollment status"};
    private static final String[] ENROLLMENT_HINTS = {ID_HINT, "Student | select", "Course | select", DATE_HINT,
            "FALL|SPRING|SUMMER YYYY", choiceHint(ENROLLMENT_STATUS)};
    private static final int[] ENROLLMENT_FIELD_STEP = stepMap(1, 2, 3);

    private static final String[] ASSESSMENT_STEPS = {"Enrollment", "Assessment setup", "Result"};
    private static final String[] ASSESSMENT_LABELS = {"Assessment ID", "Enrollment", "Assessment type",
            "Maximum score", "Obtained score", "Assessment date"};
    private static final String[] ASSESSMENT_HINTS = {ID_HINT, "Active enrollment | select", choiceHint(ASSESSMENT_TYPE),
            "double | > 0, max 1000", "double | 0 to maximum", DATE_HINT};
    private static final int[] ASSESSMENT_FIELD_STEP = stepMap(2, 2, 2);

    private static final String[] PAYMENT_STEPS = {"Payer", "Payment details", "Receipt"};
    private static final String[] PAYMENT_LABELS = {"Payment ID", "Student", "Amount", "Payment date",
            "Payment method", "Receipt number"};
    private static final String[] PAYMENT_HINTS = {ID_HINT, "Student | select", "BigDecimal | > 0 RWF", DATE_HINT,
            choiceHint(PAYMENT_METHOD), "String | unique | 4-30 chars"};
    private static final int[] PAYMENT_FIELD_STEP = stepMap(2, 3, 1);

    private static final String[] BOOK_STEPS = {"Identification", "Bibliographic details", "Stock"};
    private static final String[] BOOK_LABELS = {"Book ID", "ISBN", "Title", "Author", "Total copies", "Available copies"};
    private static final String[] BOOK_HINTS = {ID_HINT, "String | unique | 10 or 13 digits", LONG_HINT, NAME_HINT,
            "int | 0-10000", "int | 0 to total copies"};
    private static final int[] BOOK_FIELD_STEP = stepMap(2, 2, 2);

    private static final String[] LOAN_STEPS = {"Borrower", "Book", "Dates"};
    private static final String[] LOAN_LABELS = {"Loan ID", "Student", "Book", "Borrowed date", "Due date", "Returned date"};
    private static final String[] LOAN_HINTS = {ID_HINT, "Student | select", "Available book | select", DATE_HINT,
            "LocalDate | after borrowed date", "derived | set by Return Book"};
    private static final int[] LOAN_FIELD_STEP = stepMap(2, 1, 3);

    // =================================================================== main menu

    public static void main(String[] args) {
        System.out.println();
        System.out.println("Welcome to AUCA_ID - School Management System");
        System.out.println("Tip: in any form type BACK for the previous step or CANCEL to discard.");
        boolean running = true;
        while (running) {
            printMainMenu();
            int option = readInt("Select an option: ", 0, 19);
            try {
                running = route(option);
            } catch (RuntimeException e) {
                // Safety net: one failed operation must never close the application.
                System.out.println("! Unexpected problem (" + e.getClass().getSimpleName() + "): " + e.getMessage());
                System.out.println("  The operation was stopped safely. Returning to the main menu.");
            }
        }
        System.out.println("Goodbye from AUCA_ID.");
    }

    private static void printMainMenu() {
        String[] left = {" 1. Register Department", " 2. Register Student", " 3. Register Teacher",
                " 4. Register Administrator", " 5. Register Course", " 6. Register Classroom", " 7. Enroll Student",
                " 8. Record Assessment", " 9. Record Fee Payment", "10. Register Library Book"};
        String[] right = {"11. Borrow Library Book", "12. Return Library Book", "13. View All Records",
                "14. Search Record by ID", "15. Update Existing Record", "16. Student Academic Summary",
                "17. Student Payment History", "18. View Available Books", "19. Demonstrate Polymorphism", " 0. Exit"};
        System.out.println();
        System.out.println("============== AUCA_ID SCHOOL MANAGEMENT ==============");
        for (int i = 0; i < left.length; i++) {
            System.out.printf("%-28s%s%n", left[i], right[i]);
        }
    }

    /** @return false when the user confirmed Exit. */
    private static boolean route(int option) {
        switch (option) {
            case 1 -> registrationLoop("Department", () -> registerDepartment(null));
            case 2 -> registrationLoop("Student", () -> registerStudent(null));
            case 3 -> registrationLoop("Teacher", () -> registerTeacher(null));
            case 4 -> registrationLoop("Administrator", () -> registerAdministrator(null));
            case 5 -> registrationLoop("Course", () -> registerCourse(null));
            case 6 -> registrationLoop("Classroom", () -> registerClassroom(null));
            case 7 -> registrationLoop("Enrollment", () -> enrollStudent(null));
            case 8 -> registrationLoop("Assessment", () -> recordAssessment(null));
            case 9 -> registrationLoop("Fee payment", () -> recordFeePayment(null));
            case 10 -> registrationLoop("Library book", () -> registerLibraryBook(null));
            case 11 -> registrationLoop("Book loan", () -> borrowBook(null));
            case 12 -> returnBook();
            case 13 -> viewAllRecords();
            case 14 -> searchById();
            case 15 -> updateRecord();
            case 16 -> studentAcademicSummary();
            case 17 -> studentPaymentHistory();
            case 18 -> showAvailableBooks();
            case 19 -> demonstratePolymorphism();
            case 0 -> {
                return !confirm("Exit AUCA_ID? All in-memory records will be lost.");
            }
            default -> System.out.println("! Invalid option.");
        }
        return true;
    }

    /** Runs a top-level registration and offers: Register another / View / Main menu. */
    private static void registrationLoop(String entity, Supplier<Registrable> form) {
        while (true) {
            Registrable saved = form.get();
            if (saved == null) {
                System.out.println("Cancelled. Nothing was saved and no stock was changed.");
                return;
            }
            boolean again = false;
            while (!again) {
                System.out.println("  [1] Register another   [2] View " + entity.toLowerCase() + "   [0] Main menu");
                String choice = readLine("Select option: ");
                switch (choice) {
                    case "1" -> again = true;
                    case "2" -> saved.printDetails();
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("! Invalid selection: choose 1, 2 or 0.");
                }
            }
        }
    }

    // =================================================================== 1. Department

    private static Department registerDepartment(String returnTo) {
        Object[] v = new Object[DEPARTMENT_LABELS.length];
        Department department = fillForm("DEPARTMENT REGISTRATION", "department", DEPARTMENT_STEPS,
                DEPARTMENT_FIELD_STEP, DEPARTMENT_LABELS, v, new boolean[v.length],
                i -> readDepartmentField(i, v), Main::buildDepartment, false);
        if (department == null) {
            return null;
        }
        departments.add(department);
        announceSaved("Department", department, returnTo);
        return department;
    }

    private static Object readDepartmentField(int i, Object[] v) {
        String label = DEPARTMENT_LABELS[i];
        String hint = DEPARTMENT_HINTS[i];
        return switch (i) {
            case 0 -> askField(i, label, hint, v[i], raw -> uniqueId(raw, label));
            case 1 -> askField(i, label, hint, v[i], raw -> {
                String name = Registrable.validateName(raw, label);
                return requireUnique(name, departments.stream().anyMatch(d -> d != editingRecord
                        && d.getDepartmentName().equalsIgnoreCase(name)), label);
            });
            case 2 -> askField(i, label, hint, v[i], raw -> {
                String code = Registrable.validateCode(raw, label, 2, 10);
                return requireUnique(code, departments.stream().anyMatch(d -> d != editingRecord
                        && d.getDepartmentCode().equalsIgnoreCase(code)), label);
            });
            case 3 -> askField(i, label, hint, v[i], raw -> Registrable.validateName(raw, label));
            case 4 -> askField(i, label, hint, v[i], raw -> Registrable.validateEmail(raw, label));
            case 5 -> askSelect(i, label, "Teacher", () -> teachers, v[i], null, true, "department form");
            default -> throw new IllegalStateException("Unknown department field " + i);
        };
    }

    private static Department buildDepartment(Object[] v) {
        String id = (String) v[0];
        requireUnique(id, idExists(id), "ID");
        requireUnique((String) v[1], departments.stream()
                .anyMatch(d -> d.getDepartmentName().equalsIgnoreCase((String) v[1])), "Department name");
        requireUnique((String) v[2], departments.stream()
                .anyMatch(d -> d.getDepartmentCode().equalsIgnoreCase((String) v[2])), "Department code");
        Teacher head = val(v[5]);
        if (head != null && !teachers.contains(head)) {
            throw new ValidationException("Head teacher must be a registered teacher.");
        }
        return new Department(id, (String) v[1], (String) v[2], (String) v[3], (String) v[4], head);
    }

    private static void updateDepartment(Department d) {
        Object[] v = {d.getId(), d.getDepartmentName(), d.getDepartmentCode(), d.getOfficeLocation(),
                d.getContactEmail(), d.getHeadTeacher() == null ? NONE : d.getHeadTeacher()};
        editRecord("UPDATE DEPARTMENT " + d.getId(), "department", DEPARTMENT_STEPS, DEPARTMENT_FIELD_STEP,
                DEPARTMENT_LABELS, v, lockFirst(v.length), i -> readDepartmentField(i, v), vals -> {
                    d.setDepartmentName((String) vals[1]);
                    d.setDepartmentCode((String) vals[2]);
                    d.setOfficeLocation((String) vals[3]);
                    d.setContactEmail((String) vals[4]);
                    d.setHeadTeacher(val(vals[5]));
                }, d);
    }

    // =================================================================== 2. Student

    private static Student registerStudent(String returnTo) {
        Object[] v = new Object[STUDENT_LABELS.length];
        Student student = fillForm("STUDENT REGISTRATION", "student", STUDENT_STEPS, STUDENT_FIELD_STEP,
                STUDENT_LABELS, v, new boolean[v.length], i -> readStudentField(i, v, "Student registration"),
                Main::buildStudent, false);
        if (student == null) {
            return null;
        }
        students.add(student);
        announceSaved("Student", student, returnTo);
        return student;
    }

    private static Object readStudentField(int i, Object[] v, String form) {
        if (i < 6) {
            return readPersonField(i, v, STUDENT_LABELS, STUDENT_HINTS);
        }
        if (i < 12) {
            return readAcademicField(i, v, STUDENT_LABELS, STUDENT_HINTS, form);
        }
        String label = STUDENT_LABELS[i];
        String hint = STUDENT_HINTS[i];
        LocalDate dob = v[5] instanceof LocalDate d ? d : null;
        return switch (i) {
            case 12 -> askField(i, label, hint, v[i], raw -> {
                String number = Identifiable.validateId(raw, label);
                return requireUnique(number, students.stream().anyMatch(s -> s != editingRecord
                        && s.getStudentNumber().equalsIgnoreCase(number)), label);
            });
            case 13 -> askField(i, label, hint, v[i], raw -> Registrable.validateLongText(raw, label));
            case 14 -> askField(i, label, hint, v[i], raw -> notFutureNotBefore(raw, label, dob, "date of birth"));
            case 15 -> askField(i, label, hint, v[i], raw -> intInRange(raw, label, 1, 6));
            case 16 -> askField(i, label, hint, v[i], raw -> Registrable.validatePhone(raw, label));
            case 17 -> askField(i, label, hint, v[i], raw -> parseYesNo(raw, label));
            default -> throw new IllegalStateException("Unknown student field " + i);
        };
    }

    private static Student buildStudent(Object[] v) {
        Student student = new Student((String) v[0], (String) v[1], (String) v[2], (String) v[3], (String) v[4],
                (LocalDate) v[5], (String) v[6], (Department) v[7], (LocalDate) v[8], (String) v[9],
                (String) v[10], (String) v[11], (String) v[12], (String) v[13], (LocalDate) v[14],
                (Integer) v[15], (String) v[16], (Boolean) v[17]);
        checkAcademicMemberUniqueness(student);
        requireUnique(student.getStudentNumber(), students.stream()
                .anyMatch(s -> s.getStudentNumber().equalsIgnoreCase(student.getStudentNumber())), "Student number");
        return student;
    }

    private static void updateStudent(Student s) {
        Object[] v = concatValues(personValues(s), academicValues(s), new Object[] {s.getStudentNumber(),
                s.getProgramName(), s.getAdmissionDate(), s.getStudyYear(), s.getGuardianPhone(), s.isScholarshipStatus()});
        editRecord("UPDATE STUDENT " + s.getId(), "student", STUDENT_STEPS, STUDENT_FIELD_STEP, STUDENT_LABELS, v,
                lockFirst(v.length), i -> readStudentField(i, v, "Student update"), vals -> {
                    applyPerson(s, vals);
                    applyAcademic(s, vals);
                    s.setStudentNumber((String) vals[12]);
                    s.setProgramName((String) vals[13]);
                    s.setAdmissionDate((LocalDate) vals[14]);
                    s.setStudyYear((Integer) vals[15]);
                    s.setGuardianPhone((String) vals[16]);
                    s.setScholarshipStatus((Boolean) vals[17]);
                }, s);
    }

    // =================================================================== shared Person / AcademicMember fields

    private static Object readPersonField(int i, Object[] v, String[] labels, String[] hints) {
        String label = labels[i];
        String hint = hints[i];
        return switch (i) {
            case 0 -> askField(i, label, hint, v[i], raw -> uniqueId(raw, label));
            case 1, 2 -> askField(i, label, hint, v[i], raw -> Registrable.validateName(raw, label));
            case 3 -> askField(i, label, hint, v[i], raw -> {
                String email = Registrable.validateEmail(raw, label);
                return requireUnique(email, allPersons().stream().anyMatch(p -> p != editingRecord
                        && p.getEmail().equalsIgnoreCase(email)), label);
            });
            case 4 -> askField(i, label, hint, v[i], raw -> Registrable.validatePhone(raw, label));
            case 5 -> askField(i, label, hint, v[i],
                    raw -> Registrable.requirePastDate(Registrable.parseDate(raw, label), label));
            default -> throw new IllegalStateException("Unknown person field " + i);
        };
    }

    private static Object readAcademicField(int i, Object[] v, String[] labels, String[] hints, String form) {
        String label = labels[i];
        String hint = hints[i];
        LocalDate dob = v[5] instanceof LocalDate d ? d : null;
        return switch (i) {
            case 6 -> askField(i, label, hint, v[i], raw -> {
                String number = Identifiable.validateId(raw, label);
                return requireUnique(number, academicMembers().stream().anyMatch(m -> m != editingRecord
                        && m.getMemberNumber().equalsIgnoreCase(number)), label);
            });
            case 7 -> askSelect(i, label, "Department", () -> departments, v[i],
                    () -> registerDepartment(form), false, form);
            case 8 -> askField(i, label, hint, v[i], raw -> notFutureNotBefore(raw, label, dob, "date of birth"));
            case 9 -> askField(i, label, hint, v[i], raw -> pickChoice(raw, ACADEMIC_STATUS, label));
            case 10 -> askField(i, label, hint, v[i], raw -> {
                String email = Registrable.validateEmail(raw, label);
                return requireUnique(email, academicMembers().stream().anyMatch(m -> m != editingRecord
                        && m.getInstitutionalEmail().equalsIgnoreCase(email)), label);
            });
            case 11 -> askField(i, label, hint, v[i], raw -> Registrable.validateLongText(raw, label));
            default -> throw new IllegalStateException("Unknown academic field " + i);
        };
    }

    private static Object[] personValues(Person p) {
        return new Object[] {p.getId(), p.getFirstName(), p.getLastName(), p.getEmail(), p.getPhoneNumber(),
                p.getDateOfBirth()};
    }

    private static Object[] academicValues(AcademicMember m) {
        return new Object[] {m.getMemberNumber(), m.getDepartment(), m.getJoinDate(), m.getAcademicStatus(),
                m.getInstitutionalEmail(), m.getAdvisorName()};
    }

    private static void applyPerson(Person p, Object[] v) {
        p.setFirstName((String) v[1]);
        p.setLastName((String) v[2]);
        p.setEmail((String) v[3]);
        p.setPhoneNumber((String) v[4]);
        p.setDateOfBirth((LocalDate) v[5]);
    }

    private static void applyAcademic(AcademicMember m, Object[] v) {
        m.setMemberNumber((String) v[6]);
        m.setDepartment((Department) v[7]);
        m.setJoinDate((LocalDate) v[8]);
        m.setAcademicStatus((String) v[9]);
        m.setInstitutionalEmail((String) v[10]);
        m.setAdvisorName((String) v[11]);
    }

    /** Save-time re-check of person/academic unique values and the department reference. */
    private static void checkAcademicMemberUniqueness(AcademicMember m) {
        checkPersonUniqueness(m);
        requireUnique(m.getMemberNumber(), academicMembers().stream()
                .anyMatch(x -> x.getMemberNumber().equalsIgnoreCase(m.getMemberNumber())), "Member number");
        requireUnique(m.getInstitutionalEmail(), academicMembers().stream()
                .anyMatch(x -> x.getInstitutionalEmail().equalsIgnoreCase(m.getInstitutionalEmail())), "Institutional email");
        if (!departments.contains(m.getDepartment())) {
            throw new ValidationException("The selected department is not registered.");
        }
    }

    private static void checkPersonUniqueness(Person p) {
        requireUnique(p.getId(), idExists(p.getId()), "ID");
        requireUnique(p.getEmail(), allPersons().stream()
                .anyMatch(x -> x.getEmail().equalsIgnoreCase(p.getEmail())), "Email");
    }

    // =================================================================== 3. Teacher

    private static Teacher registerTeacher(String returnTo) {
        Object[] v = new Object[TEACHER_LABELS.length];
        Teacher teacher = fillForm("TEACHER REGISTRATION", "teacher", TEACHER_STEPS, TEACHER_FIELD_STEP,
                TEACHER_LABELS, v, new boolean[v.length], i -> readTeacherField(i, v, "Teacher registration"),
                Main::buildTeacher, false);
        if (teacher == null) {
            return null;
        }
        teachers.add(teacher);
        announceSaved("Teacher", teacher, returnTo);
        Department department = teacher.getDepartment();
        if (department.getHeadTeacher() == null
                && confirm("Department " + department.getDisplayName() + " has no head teacher. Assign "
                + teacher.getDisplayName() + " as head?")) {
            department.setHeadTeacher(teacher);
            System.out.println("+ " + teacher.getDisplayName() + " is now head of " + department.getDisplayName() + ".");
        }
        return teacher;
    }

    private static Object readTeacherField(int i, Object[] v, String form) {
        if (i < 6) {
            return readPersonField(i, v, TEACHER_LABELS, TEACHER_HINTS);
        }
        if (i < 12) {
            return readAcademicField(i, v, TEACHER_LABELS, TEACHER_HINTS, form);
        }
        String label = TEACHER_LABELS[i];
        String hint = TEACHER_HINTS[i];
        LocalDate dob = v[5] instanceof LocalDate d ? d : null;
        return switch (i) {
            case 12 -> askField(i, label, hint, v[i], raw -> {
                String number = Identifiable.validateId(raw, label);
                return requireUnique(number, teachers.stream().anyMatch(t -> t != editingRecord
                        && t.getEmployeeNumber().equalsIgnoreCase(number)), label);
            });
            case 13 -> askField(i, label, hint, v[i], raw -> Registrable.validateName(raw, label));
            case 14 -> askField(i, label, hint, v[i], raw -> notFutureNotBefore(raw, label, dob, "date of birth"));
            case 15 -> askField(i, label, hint, v[i], raw -> Registrable.parseMoney(raw, label));
            case 16 -> askField(i, label, hint, v[i], raw -> Registrable.validateCode(raw, label, 1, 10));
            case 17 -> askField(i, label, hint, v[i], raw -> pickChoice(raw, EMPLOYMENT_TYPE, label));
            default -> throw new IllegalStateException("Unknown teacher field " + i);
        };
    }

    private static Teacher buildTeacher(Object[] v) {
        Teacher teacher = new Teacher((String) v[0], (String) v[1], (String) v[2], (String) v[3], (String) v[4],
                (LocalDate) v[5], (String) v[6], (Department) v[7], (LocalDate) v[8], (String) v[9],
                (String) v[10], (String) v[11], (String) v[12], (String) v[13], (LocalDate) v[14],
                (BigDecimal) v[15], (String) v[16], (String) v[17]);
        checkAcademicMemberUniqueness(teacher);
        requireUnique(teacher.getEmployeeNumber(), teachers.stream()
                .anyMatch(t -> t.getEmployeeNumber().equalsIgnoreCase(teacher.getEmployeeNumber())), "Employee number");
        return teacher;
    }

    private static void updateTeacher(Teacher t) {
        Object[] v = concatValues(personValues(t), academicValues(t), new Object[] {t.getEmployeeNumber(),
                t.getSpecialization(), t.getHireDate(), t.getSalary(), t.getOfficeNumber(), t.getEmploymentType()});
        editRecord("UPDATE TEACHER " + t.getId(), "teacher", TEACHER_STEPS, TEACHER_FIELD_STEP, TEACHER_LABELS, v,
                lockFirst(v.length), i -> readTeacherField(i, v, "Teacher update"), vals -> {
                    applyPerson(t, vals);
                    applyAcademic(t, vals);
                    t.setEmployeeNumber((String) vals[12]);
                    t.setSpecialization((String) vals[13]);
                    t.setHireDate((LocalDate) vals[14]);
                    t.setSalary((BigDecimal) vals[15]);
                    t.setOfficeNumber((String) vals[16]);
                    t.setEmploymentType((String) vals[17]);
                }, t);
    }

    // =================================================================== 4. Administrator

    private static Administrator registerAdministrator(String returnTo) {
        Object[] v = new Object[ADMIN_LABELS.length];
        Administrator admin = fillForm("ADMINISTRATOR REGISTRATION", "administrator", ADMIN_STEPS, ADMIN_FIELD_STEP,
                ADMIN_LABELS, v, new boolean[v.length], i -> readAdministratorField(i, v),
                Main::buildAdministrator, false);
        if (admin == null) {
            return null;
        }
        administrators.add(admin);
        announceSaved("Administrator", admin, returnTo);
        return admin;
    }

    private static Object readAdministratorField(int i, Object[] v) {
        if (i < 6) {
            return readPersonField(i, v, ADMIN_LABELS, ADMIN_HINTS);
        }
        String label = ADMIN_LABELS[i];
        String hint = ADMIN_HINTS[i];
        LocalDate dob = v[5] instanceof LocalDate d ? d : null;
        return switch (i) {
            case 6 -> askField(i, label, hint, v[i], raw -> {
                String number = Identifiable.validateId(raw, label);
                return requireUnique(number, administrators.stream().anyMatch(a -> a != editingRecord
                        && a.getStaffNumber().equalsIgnoreCase(number)), label);
            });
            case 7, 8 -> askField(i, label, hint, v[i], raw -> Registrable.validateName(raw, label));
            case 9 -> askField(i, label, hint, v[i], raw -> notFutureNotBefore(raw, label, dob, "date of birth"));
            case 10 -> askField(i, label, hint, v[i], raw -> intInRange(raw, label, 1, 5));
            case 11 -> askField(i, label, hint, v[i], raw -> parseYesNo(raw, label));
            default -> throw new IllegalStateException("Unknown administrator field " + i);
        };
    }

    private static Administrator buildAdministrator(Object[] v) {
        Administrator admin = new Administrator((String) v[0], (String) v[1], (String) v[2], (String) v[3],
                (String) v[4], (LocalDate) v[5], (String) v[6], (String) v[7], (String) v[8], (LocalDate) v[9],
                (Integer) v[10], (Boolean) v[11]);
        checkPersonUniqueness(admin);
        requireUnique(admin.getStaffNumber(), administrators.stream()
                .anyMatch(a -> a.getStaffNumber().equalsIgnoreCase(admin.getStaffNumber())), "Staff number");
        return admin;
    }

    private static void updateAdministrator(Administrator a) {
        Object[] v = concatValues(personValues(a), new Object[] {a.getStaffNumber(), a.getJobTitle(),
                a.getAssignedOffice(), a.getEmploymentDate(), a.getAccessLevel(), a.isActive()});
        editRecord("UPDATE ADMINISTRATOR " + a.getId(), "administrator", ADMIN_STEPS, ADMIN_FIELD_STEP, ADMIN_LABELS,
                v, lockFirst(v.length), i -> readAdministratorField(i, v), vals -> {
                    applyPerson(a, vals);
                    a.setStaffNumber((String) vals[6]);
                    a.setJobTitle((String) vals[7]);
                    a.setAssignedOffice((String) vals[8]);
                    a.setEmploymentDate((LocalDate) vals[9]);
                    a.setAccessLevel((Integer) vals[10]);
                    a.setActive((Boolean) vals[11]);
                }, a);
    }

    // =================================================================== 5. Course

    private static Course registerCourse(String returnTo) {
        String form = "Course registration";
        if (!ensurePrerequisite("Department", "registering a course", () -> departments,
                () -> registerDepartment(form), form)
                || !ensurePrerequisite("Teacher", "registering a course", () -> teachers,
                () -> registerTeacher(form), form)) {
            return null;
        }
        Object[] v = new Object[COURSE_LABELS.length];
        Course course = fillForm("COURSE REGISTRATION", "course", COURSE_STEPS, COURSE_FIELD_STEP, COURSE_LABELS, v,
                new boolean[v.length], i -> readCourseField(i, v, form), Main::buildCourse, false);
        if (course == null) {
            return null;
        }
        courses.add(course);
        announceSaved("Course", course, returnTo);
        return course;
    }

    private static Object readCourseField(int i, Object[] v, String form) {
        String label = COURSE_LABELS[i];
        String hint = COURSE_HINTS[i];
        return switch (i) {
            case 0 -> askField(i, label, hint, v[i], raw -> uniqueId(raw, label));
            case 1 -> askField(i, label, hint, v[i], raw -> {
                String code = Registrable.validateCode(raw, label, 3, 12);
                return requireUnique(code, courses.stream().anyMatch(c -> c != editingRecord
                        && c.getCourseCode().equalsIgnoreCase(code)), label);
            });
            case 2 -> askField(i, label, hint, v[i], raw -> Registrable.validateLongText(raw, label));
            case 3 -> askField(i, label, hint, v[i], raw -> intInRange(raw, label, 1, 6));
            case 4 -> askSelect(i, label, "Department", () -> departments, v[i],
                    () -> registerDepartment(form), false, form);
            case 5 -> askSelect(i, label, "Teacher", () -> teachers, v[i], () -> registerTeacher(form), false, form);
            default -> throw new IllegalStateException("Unknown course field " + i);
        };
    }

    private static Course buildCourse(Object[] v) {
        String id = (String) v[0];
        requireUnique(id, idExists(id), "ID");
        requireUnique((String) v[1], courses.stream()
                .anyMatch(c -> c.getCourseCode().equalsIgnoreCase((String) v[1])), "Course code");
        Course course = new Course(id, (String) v[1], (String) v[2], (Integer) v[3], (Department) v[4], (Teacher) v[5]);
        checkCourseReferences(course);
        return course;
    }

    private static void checkCourseReferences(Course c) {
        if (!departments.contains(c.getDepartment())) {
            throw new ValidationException("A course must reference a registered department.");
        }
        if (!teachers.contains(c.getAssignedTeacher())) {
            throw new ValidationException("A course must reference a registered teacher.");
        }
    }

    private static void updateCourse(Course c) {
        Object[] v = {c.getId(), c.getCourseCode(), c.getCourseTitle(), c.getCreditHours(), c.getDepartment(),
                c.getAssignedTeacher()};
        editRecord("UPDATE COURSE " + c.getId(), "course", COURSE_STEPS, COURSE_FIELD_STEP, COURSE_LABELS, v,
                lockFirst(v.length), i -> readCourseField(i, v, "Course update"), vals -> {
                    c.setCourseCode((String) vals[1]);
                    c.setCourseTitle((String) vals[2]);
                    c.setCreditHours((Integer) vals[3]);
                    c.setDepartment((Department) vals[4]);
                    c.setAssignedTeacher((Teacher) vals[5]);
                    checkCourseReferences(c);
                }, c);
    }

    // =================================================================== 6. Classroom

    private static Classroom registerClassroom(String returnTo) {
        Object[] v = new Object[CLASSROOM_LABELS.length];
        Classroom room = fillForm("CLASSROOM REGISTRATION", "classroom", CLASSROOM_STEPS, CLASSROOM_FIELD_STEP,
                CLASSROOM_LABELS, v, new boolean[v.length], i -> readClassroomField(i, v), Main::buildClassroom, false);
        if (room == null) {
            return null;
        }
        classrooms.add(room);
        announceSaved("Classroom", room, returnTo);
        return room;
    }

    private static Object readClassroomField(int i, Object[] v) {
        String label = CLASSROOM_LABELS[i];
        String hint = CLASSROOM_HINTS[i];
        return switch (i) {
            case 0 -> askField(i, label, hint, v[i], raw -> uniqueId(raw, label));
            case 1 -> askField(i, label, hint, v[i], raw -> Registrable.validateName(raw, label));
            case 2 -> askField(i, label, hint, v[i], raw -> Registrable.validateCode(raw, label, 1, 10));
            case 3 -> askField(i, label, hint, v[i], raw -> intInRange(raw, label, 1, 1000));
            case 4 -> askField(i, label, hint, v[i], raw -> intInRange(raw, label, 0, 200));
            case 5 -> askField(i, label, hint, v[i], raw -> parseYesNo(raw, label));
            default -> throw new IllegalStateException("Unknown classroom field " + i);
        };
    }

    private static Classroom buildClassroom(Object[] v) {
        String id = (String) v[0];
        requireUnique(id, idExists(id), "ID");
        Classroom room = new Classroom(id, (String) v[1], (String) v[2], (Integer) v[3], (Integer) v[4], (Boolean) v[5]);
        checkClassroomLocation(room);
        return room;
    }

    private static void checkClassroomLocation(Classroom room) {
        boolean clash = classrooms.stream().anyMatch(c -> c != room
                && c.getBuildingName().equalsIgnoreCase(room.getBuildingName())
                && c.getRoomNumber().equalsIgnoreCase(room.getRoomNumber()));
        if (clash) {
            throw new DuplicateRecordException("Room " + room.getRoomNumber() + " in " + room.getBuildingName()
                    + " is already registered.");
        }
    }

    private static void updateClassroom(Classroom c) {
        Object[] v = {c.getId(), c.getBuildingName(), c.getRoomNumber(), c.getCapacity(), c.getFloorNumber(),
                c.isAvailable()};
        editRecord("UPDATE CLASSROOM " + c.getId(), "classroom", CLASSROOM_STEPS, CLASSROOM_FIELD_STEP,
                CLASSROOM_LABELS, v, lockFirst(v.length), i -> readClassroomField(i, v), vals -> {
                    c.setBuildingName((String) vals[1]);
                    c.setRoomNumber((String) vals[2]);
                    c.setCapacity((Integer) vals[3]);
                    c.setFloorNumber((Integer) vals[4]);
                    c.setAvailable((Boolean) vals[5]);
                    checkClassroomLocation(c);
                }, c);
    }

    // =================================================================== 7. Enrollment

    private static Enrollment enrollStudent(String returnTo) {
        String form = "Enrollment";
        if (!ensurePrerequisite("Student", "enrolling a student", () -> students, () -> registerStudent(form), form)
                || !ensurePrerequisite("Course", "enrolling a student", () -> courses,
                () -> registerCourse(form), form)) {
            return null;
        }
        Object[] v = new Object[ENROLLMENT_LABELS.length];
        Enrollment enrollment = fillForm("ENROLL STUDENT", "enrollment", ENROLLMENT_STEPS, ENROLLMENT_FIELD_STEP,
                ENROLLMENT_LABELS, v, new boolean[v.length], i -> readEnrollmentField(i, v, form),
                Main::buildEnrollment, false);
        if (enrollment == null) {
            return null;
        }
        enrollments.add(enrollment);
        announceSaved("Enrollment", enrollment, returnTo);
        return enrollment;
    }

    private static Object readEnrollmentField(int i, Object[] v, String form) {
        String label = ENROLLMENT_LABELS[i];
        String hint = ENROLLMENT_HINTS[i];
        return switch (i) {
            case 0 -> askField(i, label, hint, v[i], raw -> uniqueId(raw, label));
            case 1 -> askSelect(i, label, "Student", () -> students, v[i], () -> registerStudent(form), false, form);
            case 2 -> askSelect(i, label, "Course", () -> courses, v[i], () -> registerCourse(form), false, form);
            case 3 -> askField(i, label, hint, v[i], raw -> notFutureNotBefore(raw, label, null, null));
            case 4 -> askField(i, label, hint, v[i], raw -> Registrable.validateSemester(raw, label));
            case 5 -> askField(i, label, hint, v[i], raw -> pickChoice(raw, ENROLLMENT_STATUS, label));
            default -> throw new IllegalStateException("Unknown enrollment field " + i);
        };
    }

    private static Enrollment buildEnrollment(Object[] v) {
        String id = (String) v[0];
        requireUnique(id, idExists(id), "ID");
        Enrollment e = new Enrollment(id, (Student) v[1], (Course) v[2], (LocalDate) v[3], (String) v[4], (String) v[5]);
        checkEnrollmentRules(e);
        return e;
    }

    /** Student and Course must exist; no second ACTIVE enrollment for the same student, course and semester. */
    private static void checkEnrollmentRules(Enrollment e) {
        if (!students.contains(e.getStudent()) || !courses.contains(e.getCourse())) {
            throw new ValidationException("Enrollment must reference a registered student and course.");
        }
        if (e.isActive() && enrollments.stream().anyMatch(x -> x != e && x.isActive()
                && x.getStudent() == e.getStudent() && x.getCourse() == e.getCourse()
                && x.getSemester().equals(e.getSemester()))) {
            throw new DuplicateRecordException(e.getStudent().getDisplayName() + " already has an ACTIVE enrollment in "
                    + e.getCourse().getCourseCode() + " for " + e.getSemester() + ".");
        }
    }

    private static void updateEnrollment(Enrollment e) {
        Object[] v = {e.getId(), e.getStudent(), e.getCourse(), e.getEnrollmentDate(), e.getSemester(),
                e.getEnrollmentStatus()};
        boolean[] locked = {true, true, true, false, false, false}; // student/course fixed: assessments depend on them
        editRecord("UPDATE ENROLLMENT " + e.getId(), "enrollment", ENROLLMENT_STEPS, ENROLLMENT_FIELD_STEP,
                ENROLLMENT_LABELS, v, locked, i -> readEnrollmentField(i, v, "Enrollment update"), vals -> {
                    e.setEnrollmentDate((LocalDate) vals[3]);
                    e.setSemester((String) vals[4]);
                    e.setEnrollmentStatus((String) vals[5]);
                    checkEnrollmentRules(e);
                }, e);
    }

    // =================================================================== 8. Assessment

    private static Assessment recordAssessment(String returnTo) {
        String form = "Assessment";
        if (!ensurePrerequisite("active Enrollment", "recording an assessment", Main::activeEnrollments,
                () -> enrollStudent(form), form)) {
            return null;
        }
        Object[] v = new Object[ASSESSMENT_LABELS.length];
        Assessment assessment = fillForm("RECORD ASSESSMENT", "assessment", ASSESSMENT_STEPS, ASSESSMENT_FIELD_STEP,
                ASSESSMENT_LABELS, v, new boolean[v.length], i -> readAssessmentField(i, v, form),
                Main::buildAssessment, false);
        if (assessment == null) {
            return null;
        }
        assessments.add(assessment);
        announceSaved("Assessment", assessment, returnTo);
        return assessment;
    }

    private static Object readAssessmentField(int i, Object[] v, String form) {
        String label = ASSESSMENT_LABELS[i];
        String hint = ASSESSMENT_HINTS[i];
        return switch (i) {
            case 0 -> askField(i, label, hint, v[i], raw -> uniqueId(raw, label));
            case 1 -> askSelect(i, label, "active Enrollment", Main::activeEnrollments, v[i],
                    () -> enrollStudent(form), false, form);
            case 2 -> askField(i, label, hint, v[i], raw -> pickChoice(raw, ASSESSMENT_TYPE, label));
            case 3 -> askField(i, label, hint, v[i], raw -> {
                double max = parseScore(raw, label);
                if (max <= 0 || max > 1000) {
                    throw new ValidationException(label + " must be greater than 0 and at most 1000.");
                }
                return max;
            });
            case 4 -> askField(i, label, hint, v[i], raw -> {
                double obtained = parseScore(raw, label);
                double max = v[3] instanceof Double d ? d : Double.MAX_VALUE;
                if (obtained < 0 || obtained > max) {
                    throw new ValidationException(label + " must be between 0 and the maximum score (" + max + ").");
                }
                return obtained;
            });
            case 5 -> askField(i, label, hint, v[i], raw -> notFutureNotBefore(raw, label,
                    v[1] instanceof Enrollment e ? e.getEnrollmentDate() : null, "enrollment date"));
            default -> throw new IllegalStateException("Unknown assessment field " + i);
        };
    }

    private static Assessment buildAssessment(Object[] v) {
        String id = (String) v[0];
        requireUnique(id, idExists(id), "ID");
        Enrollment enrollment = (Enrollment) v[1];
        if (!enrollment.isActive()) {
            throw new ValidationException("Only an ACTIVE enrollment can be assessed ("
                    + enrollment.getId() + " is " + enrollment.getEnrollmentStatus() + ").");
        }
        return new Assessment(id, enrollment, (String) v[2], (Double) v[3], (Double) v[4], (LocalDate) v[5]);
    }

    private static void updateAssessment(Assessment a) {
        Object[] v = {a.getId(), a.getEnrollment(), a.getAssessmentType(), a.getMaximumScore(), a.getObtainedScore(),
                a.getAssessmentDate()};
        boolean[] locked = {true, true, false, false, false, false};
        editRecord("UPDATE ASSESSMENT " + a.getId(), "assessment", ASSESSMENT_STEPS, ASSESSMENT_FIELD_STEP,
                ASSESSMENT_LABELS, v, locked, i -> readAssessmentField(i, v, "Assessment update"), vals -> {
                    a.setAssessmentType((String) vals[2]);
                    a.setMaximumScore((Double) vals[3]);
                    a.setObtainedScore((Double) vals[4]);
                    a.setAssessmentDate((LocalDate) vals[5]);
                }, a);
    }

    // =================================================================== 9. Fee payment

    private static FeePayment recordFeePayment(String returnTo) {
        String form = "Fee payment";
        if (!ensurePrerequisite("Student", "recording a fee payment", () -> students,
                () -> registerStudent(form), form)) {
            return null;
        }
        Object[] v = new Object[PAYMENT_LABELS.length];
        FeePayment payment = fillForm("RECORD FEE PAYMENT", "fee payment", PAYMENT_STEPS, PAYMENT_FIELD_STEP,
                PAYMENT_LABELS, v, new boolean[v.length], i -> readPaymentField(i, v, form), Main::buildPayment, false);
        if (payment == null) {
            return null;
        }
        payments.add(payment);
        announceSaved("Fee payment", payment, returnTo);
        return payment;
    }

    private static Object readPaymentField(int i, Object[] v, String form) {
        String label = PAYMENT_LABELS[i];
        String hint = PAYMENT_HINTS[i];
        return switch (i) {
            case 0 -> askField(i, label, hint, v[i], raw -> uniqueId(raw, label));
            case 1 -> askSelect(i, label, "Student", () -> students, v[i], () -> registerStudent(form), false, form);
            case 2 -> askField(i, label, hint, v[i], raw -> Registrable.parseMoney(raw, label));
            case 3 -> askField(i, label, hint, v[i], raw -> notFutureNotBefore(raw, label, null, null));
            case 4 -> askField(i, label, hint, v[i], raw -> pickChoice(raw, PAYMENT_METHOD, label));
            case 5 -> askField(i, label, hint, v[i], raw -> {
                String receipt = Identifiable.validateId(raw, label);
                return requireUnique(receipt, payments.stream().anyMatch(p -> p != editingRecord
                        && p.getReceiptNumber().equalsIgnoreCase(receipt)), label);
            });
            default -> throw new IllegalStateException("Unknown payment field " + i);
        };
    }

    private static FeePayment buildPayment(Object[] v) {
        String id = (String) v[0];
        requireUnique(id, idExists(id), "ID");
        requireUnique((String) v[5], payments.stream()
                .anyMatch(p -> p.getReceiptNumber().equalsIgnoreCase((String) v[5])), "Receipt number");
        if (!students.contains((Student) v[1])) {
            throw new ValidationException("Payment must reference a registered student.");
        }
        return new FeePayment(id, (Student) v[1], (BigDecimal) v[2], (LocalDate) v[3], (String) v[4], (String) v[5]);
    }

    private static void updatePayment(FeePayment p) {
        Object[] v = {p.getId(), p.getStudent(), p.getAmount(), p.getPaymentDate(), p.getPaymentMethod(),
                p.getReceiptNumber()};
        boolean[] locked = {true, true, false, false, false, false};
        editRecord("UPDATE FEE PAYMENT " + p.getId(), "fee payment", PAYMENT_STEPS, PAYMENT_FIELD_STEP,
                PAYMENT_LABELS, v, locked, i -> readPaymentField(i, v, "Payment update"), vals -> {
                    p.setAmount((BigDecimal) vals[2]);
                    p.setPaymentDate((LocalDate) vals[3]);
                    p.setPaymentMethod((String) vals[4]);
                    p.setReceiptNumber((String) vals[5]);
                }, p);
    }

    // =================================================================== 10. Library book

    private static LibraryBook registerLibraryBook(String returnTo) {
        Object[] v = new Object[BOOK_LABELS.length];
        LibraryBook book = fillForm("LIBRARY BOOK REGISTRATION", "library book", BOOK_STEPS, BOOK_FIELD_STEP,
                BOOK_LABELS, v, new boolean[v.length], i -> readBookField(i, v), Main::buildBook, false);
        if (book == null) {
            return null;
        }
        books.add(book);
        announceSaved("Library book", book, returnTo);
        return book;
    }

    private static Object readBookField(int i, Object[] v) {
        String label = BOOK_LABELS[i];
        String hint = BOOK_HINTS[i];
        return switch (i) {
            case 0 -> askField(i, label, hint, v[i], raw -> uniqueId(raw, label));
            case 1 -> askField(i, label, hint, v[i], raw -> {
                String isbn = Registrable.validateIsbn(raw, label);
                return requireUnique(isbn, books.stream().anyMatch(b -> b != editingRecord
                        && b.getIsbn().equals(isbn)), label);
            });
            case 2 -> askField(i, label, hint, v[i], raw -> Registrable.validateLongText(raw, label));
            case 3 -> askField(i, label, hint, v[i], raw -> Registrable.validateName(raw, label));
            case 4 -> askField(i, label, hint, v[i], raw -> intInRange(raw, label, 0, 10000));
            case 5 -> askField(i, label, hint, v[i],
                    raw -> intInRange(raw, label, 0, v[4] instanceof Integer total ? total : 10000));
            default -> throw new IllegalStateException("Unknown book field " + i);
        };
    }

    private static LibraryBook buildBook(Object[] v) {
        String id = (String) v[0];
        requireUnique(id, idExists(id), "ID");
        requireUnique((String) v[1], books.stream().anyMatch(b -> b.getIsbn().equals(v[1])), "ISBN");
        return new LibraryBook(id, (String) v[1], (String) v[2], (String) v[3], (Integer) v[4], (Integer) v[5]);
    }

    private static void updateBook(LibraryBook b) {
        Object[] v = {b.getId(), b.getIsbn(), b.getTitle(), b.getAuthor(), b.getTotalCopies(), b.getAvailableCopies()};
        editRecord("UPDATE LIBRARY BOOK " + b.getId(), "library book", BOOK_STEPS, BOOK_FIELD_STEP, BOOK_LABELS, v,
                lockFirst(v.length), i -> readBookField(i, v), vals -> {
                    b.setIsbn((String) vals[1]);
                    b.setTitle((String) vals[2]);
                    b.setAuthor((String) vals[3]);
                    b.setTotalCopies((Integer) vals[4]);
                    b.setAvailableCopies((Integer) vals[5]);
                    long onLoan = loans.stream().filter(l -> l.getBook() == b && l.isActive()).count();
                    if (b.getTotalCopies() - b.getAvailableCopies() < onLoan) {
                        throw new ValidationException(onLoan + " copies are on loan, so available copies can be at most "
                                + (b.getTotalCopies() - onLoan) + ".");
                    }
                }, b);
    }

    // =================================================================== 11. Borrow book (creates BookLoan)

    private static BookLoan borrowBook(String returnTo) {
        String form = "Borrow book";
        if (!ensurePrerequisite("Student", "borrowing a book", () -> students, () -> registerStudent(form), form)
                || !ensurePrerequisite("Library Book", "borrowing a book", () -> books,
                () -> registerLibraryBook(form), form)
                || !ensureBookAvailable(form)) {
            return null;
        }
        Object[] v = new Object[LOAN_LABELS.length];
        boolean review = false;
        while (true) {
            BookLoan loan = fillForm("BORROW LIBRARY BOOK", "book loan", LOAN_STEPS, LOAN_FIELD_STEP, LOAN_LABELS, v,
                    new boolean[v.length], i -> readLoanField(i, v, form), Main::buildLoan, review);
            if (loan == null) {
                return null;
            }
            try {
                lendCopy(loan.getBook());           // stock changes exactly once, only after validation passed
                loans.add(loan);
                announceSaved("Book loan", loan, returnTo);
                System.out.println("  '" + loan.getBook().getTitle() + "' now has " + loan.getBook().getAvailableCopies()
                        + " of " + loan.getBook().getTotalCopies() + " copies available.");
                return loan;
            } catch (BookUnavailableException e) {
                System.out.println("! " + e.getMessage());
                System.out.println("  Edit field 03 to choose another book, or cancel.");
                review = true;
            }
        }
    }

    private static Object readLoanField(int i, Object[] v, String form) {
        String label = LOAN_LABELS[i];
        String hint = LOAN_HINTS[i];
        return switch (i) {
            case 0 -> askField(i, label, hint, v[i], raw -> uniqueId(raw, label));
            case 1 -> askSelect(i, label, "Student", () -> students, v[i], () -> registerStudent(form), false, form);
            case 2 -> askSelect(i, label, "available Library Book", Main::availableBooks, v[i],
                    () -> registerLibraryBook(form), false, form);
            case 3 -> askField(i, label, hint, v[i], raw -> notFutureNotBefore(raw, label, null, null));
            case 4 -> askField(i, label, hint, v[i], raw -> {
                LocalDate due = Registrable.parseDate(raw, label);
                if (v[3] instanceof LocalDate borrowed && !due.isAfter(borrowed)) {
                    throw new ValidationException(label + " must be after the borrowed date ("
                            + Registrable.formatDate(borrowed) + ").");
                }
                return due;
            });
            case 5 -> {
                // returnedDate is derived: it stays empty until "12 Return Library Book".
                System.out.printf("%02d  %-20s [%s] : %s%n", i + 1, label, hint,
                        v[i] instanceof LocalDate d ? Registrable.formatDate(d) : "Not returned yet");
                yield v[i] == null ? NONE : v[i];
            }
            default -> throw new IllegalStateException("Unknown loan field " + i);
        };
    }

    private static BookLoan buildLoan(Object[] v) {
        String id = (String) v[0];
        requireUnique(id, idExists(id), "ID");
        Student student = (Student) v[1];
        LibraryBook book = (LibraryBook) v[2];
        if (loans.stream().anyMatch(l -> l.isActive() && l.getStudent() == student && l.getBook() == book)) {
            throw new DuplicateRecordException(student.getDisplayName() + " already has an unreturned copy of '"
                    + book.getTitle() + "'.");
        }
        return new BookLoan(id, student, book, (LocalDate) v[3], (LocalDate) v[4]);
    }

    private static void lendCopy(LibraryBook book) throws BookUnavailableException {
        if (book.getAvailableCopies() <= 0) {
            throw new BookUnavailableException("No copies of '" + book.getTitle() + "' remain. A student cannot "
                    + "borrow a book when no copies are available.");
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
    }

    private static boolean ensureBookAvailable(String form) {
        while (true) {
            try {
                if (availableBooks().isEmpty()) {
                    throw new BookUnavailableException("Every registered book is fully on loan (0 copies available).");
                }
                return true;
            } catch (BookUnavailableException e) {
                System.out.println("! " + e.getMessage());
                System.out.println("  1 Register a new Library Book now   2 Return to menu");
                if (readInt("Select: ", 0, 2) != 1) {
                    return false;
                }
                nestedRegistration("Library Book", () -> registerLibraryBook(form), form);
            }
        }
    }

    private static void updateLoan(BookLoan l) {
        Object[] v = {l.getId(), l.getStudent(), l.getBook(), l.getBorrowedDate(), l.getDueDate(),
                l.getReturnedDate() == null ? NONE : l.getReturnedDate()};
        boolean[] locked = {true, true, true, false, false, true};
        editRecord("UPDATE BOOK LOAN " + l.getId(), "book loan", LOAN_STEPS, LOAN_FIELD_STEP, LOAN_LABELS, v, locked,
                i -> readLoanField(i, v, "Loan update"), vals -> {
                    l.setBorrowedDate((LocalDate) vals[3]);
                    l.setDueDate((LocalDate) vals[4]);
                }, l);
    }

    // =================================================================== 12. Return book

    private static void returnBook() {
        List<BookLoan> active = loans.stream().filter(BookLoan::isActive).toList();
        if (active.isEmpty()) {
            System.out.println("! There are no active book loans. Nothing to return.");
            return;
        }
        BookLoan loan = selectFromList("RETURN LIBRARY BOOK", "active loan", active);
        if (loan == null) {
            System.out.println("Return cancelled. Stock unchanged.");
            return;
        }
        loan.printDetails();
        LocalDate returned;
        while (true) {
            String raw = readLine("Returned date [LocalDate | YYYY-MM-DD] (CANCEL to abort): ");
            if (isCancel(raw) || raw.equals("0")) {
                System.out.println("Return cancelled. Stock unchanged.");
                return;
            }
            try {
                returned = notFutureNotBefore(raw, "Returned date", loan.getBorrowedDate(), "borrowed date");
                break;
            } catch (DateTimeParseException e) {
                System.out.println("! Invalid date: use YYYY-MM-DD (example: 2026-11-18).");
            } catch (ValidationException e) {
                System.out.println("! " + e.getMessage());
            }
        }
        if (!confirm("Confirm return of '" + loan.getBook().getTitle() + "' by " + loan.getStudent().getDisplayName()
                + " on " + Registrable.formatDate(returned) + "?")) {
            System.out.println("Return cancelled. Stock unchanged.");
            return;
        }
        try {
            receiveCopy(loan, returned);
            System.out.println("+ Loan " + loan.getId() + " closed. '" + loan.getBook().getTitle() + "' now has "
                    + loan.getBook().getAvailableCopies() + " of " + loan.getBook().getTotalCopies() + " copies available.");
            if (returned.isAfter(loan.getDueDate())) {
                System.out.println("  Note: returned " + ChronoUnit.DAYS.between(loan.getDueDate(), returned)
                        + " day(s) after the due date.");
            }
        } catch (ValidationException e) {
            System.out.println("! Return failed: " + e.getMessage() + " Nothing was changed.");
        }
    }

    /** Closes the existing loan and increments stock exactly once; rolls back if validation fails. */
    private static void receiveCopy(BookLoan loan, LocalDate returned) {
        LibraryBook book = loan.getBook();
        if (!loan.isActive()) {
            throw new ValidationException("Loan " + loan.getId() + " was already returned.");
        }
        if (book.getAvailableCopies() >= book.getTotalCopies()) {
            throw new ValidationException("Stock inconsistency: all copies of '" + book.getTitle() + "' are already on the shelf.");
        }
        loan.setReturnedDate(returned);
        try {
            loan.validate();
        } catch (ValidationException e) {
            loan.setReturnedDate(null);
            throw e;
        }
        book.setAvailableCopies(book.getAvailableCopies() + 1);
    }

    // =================================================================== 13. View all records

    private static void viewAllRecords() {
        while (true) {
            System.out.println();
            System.out.println("------------------ VIEW ALL RECORDS ------------------");
            System.out.println(" 1. Departments (" + departments.size() + ")      7. Enrollments (" + enrollments.size() + ")");
            System.out.println(" 2. Students (" + students.size() + ")         8. Assessments (" + assessments.size() + ")");
            System.out.println(" 3. Teachers (" + teachers.size() + ")         9. Fee payments (" + payments.size() + ")");
            System.out.println(" 4. Administrators (" + administrators.size() + ")  10. Library books (" + books.size() + ")");
            System.out.println(" 5. Courses (" + courses.size() + ")         11. Book loans (" + loans.size() + ")");
            System.out.println(" 6. Classrooms (" + classrooms.size() + ")      12. Everything");
            System.out.println(" 0. Back to main menu");
            int choice = readInt("Select: ", 0, 12);
            switch (choice) {
                case 0 -> {
                    return;
                }
                case 1 -> printList("Departments", departments);
                case 2 -> printList("Students", students);
                case 3 -> printList("Teachers", teachers);
                case 4 -> printList("Administrators", administrators);
                case 5 -> printList("Courses", courses);
                case 6 -> printList("Classrooms", classrooms);
                case 7 -> printList("Enrollments", enrollments);
                case 8 -> printList("Assessments", assessments);
                case 9 -> printList("Fee payments", payments);
                case 10 -> printList("Library books", books);
                case 11 -> printList("Book loans", loans);
                default -> {
                    printList("Departments", departments);
                    printList("Students", students);
                    printList("Teachers", teachers);
                    printList("Administrators", administrators);
                    printList("Courses", courses);
                    printList("Classrooms", classrooms);
                    printList("Enrollments", enrollments);
                    printList("Assessments", assessments);
                    printList("Fee payments", payments);
                    printList("Library books", books);
                    printList("Book loans", loans);
                }
            }
            String id = readLine("Enter an ID to see full details, or press ENTER to continue: ");
            if (!id.isEmpty()) {
                try {
                    findById(id).printDetails();
                } catch (RecordNotFoundException e) {
                    System.out.println("! " + e.getMessage());
                }
            }
        }
    }

    private static void printList(String title, List<? extends Registrable> records) {
        System.out.println("== " + title + " ==");
        if (records.isEmpty()) {
            System.out.println("   (no " + title.toLowerCase() + " registered yet)");
            return;
        }
        for (Registrable r : records) {
            System.out.println("   " + r.getRegistrationSummary());
        }
    }

    // =================================================================== 14. Search by ID

    private static void searchById() {
        while (true) {
            String id = readLine("Enter the ID to search (0 to return): ");
            if (id.equals("0") || isCancel(id)) {
                return;
            }
            if (id.isEmpty()) {
                System.out.println("! Please type an ID.");
                continue;
            }
            try {
                findById(id).printDetails();
                return;
            } catch (RecordNotFoundException e) {
                System.out.println("! " + e.getMessage());
            }
        }
    }

    private static Registrable findById(String id) throws RecordNotFoundException {
        for (Identifiable record : allRecords()) {
            if (record.getId().equalsIgnoreCase(id.trim())) {
                return (Registrable) record;
            }
        }
        throw new RecordNotFoundException("No record with ID '" + id.trim() + "' exists. Check the ID and try again.");
    }

    // =================================================================== 15. Update record

    private static void updateRecord() {
        if (allRecords().isEmpty()) {
            System.out.println("! There are no records to update yet.");
            return;
        }
        Registrable record;
        while (true) {
            String id = readLine("Enter the ID of the record to update (0 to return): ");
            if (id.equals("0") || isCancel(id)) {
                return;
            }
            try {
                record = findById(id);
                break;
            } catch (RecordNotFoundException e) {
                System.out.println("! " + e.getMessage());
            }
        }
        record.printDetails();
        if (record instanceof Student s) {
            updateStudent(s);
        } else if (record instanceof Teacher t) {
            updateTeacher(t);
        } else if (record instanceof Administrator a) {
            updateAdministrator(a);
        } else if (record instanceof Department d) {
            updateDepartment(d);
        } else if (record instanceof Course c) {
            updateCourse(c);
        } else if (record instanceof Classroom c) {
            updateClassroom(c);
        } else if (record instanceof Enrollment e) {
            updateEnrollment(e);
        } else if (record instanceof Assessment a) {
            updateAssessment(a);
        } else if (record instanceof FeePayment p) {
            updatePayment(p);
        } else if (record instanceof LibraryBook b) {
            updateBook(b);
        } else if (record instanceof BookLoan l) {
            updateLoan(l);
        }
    }

    // =================================================================== 16. Student academic summary

    private static void studentAcademicSummary() {
        if (students.isEmpty()) {
            System.out.println("! No students are registered yet.");
            return;
        }
        Student s = selectFromList("STUDENT ACADEMIC SUMMARY", "student", students);
        if (s == null) {
            return;
        }
        Registrable.printHeading("ACADEMIC SUMMARY - " + s.getDisplayName());
        Registrable.printRow("Program", s.getProgramName() + ", year " + s.getStudyYear());
        Registrable.printRow("Department", Registrable.describe(s.getDepartment(), "-"));
        Registrable.printRow("Category", s.getAcademicCategory());
        Registrable.printRow("Academic status", s.getAcademicStatus());
        List<Enrollment> mine = enrollments.stream().filter(e -> e.getStudent() == s).toList();
        if (mine.isEmpty()) {
            System.out.println("  No enrollments yet.");
        }
        int credits = 0;
        double totalObtained = 0;
        double totalMax = 0;
        for (Enrollment e : mine) {
            Course c = e.getCourse();
            System.out.printf("  %s %-30s %d cr | %s | %s%n", "[" + e.getId() + "]", c.getDisplayName(),
                    c.getCreditHours(), e.getSemester(), e.getEnrollmentStatus());
            if (!"DROPPED".equals(e.getEnrollmentStatus())) {
                credits += c.getCreditHours();
            }
            double obtained = 0;
            double max = 0;
            for (Assessment a : assessments) {
                if (a.getEnrollment() == e) {
                    System.out.printf("      - %-11s %6.1f / %-6.1f (%s)%n", a.getAssessmentType(), a.getObtainedScore(),
                            a.getMaximumScore(), Registrable.formatDate(a.getAssessmentDate()));
                    obtained += a.getObtainedScore();
                    max += a.getMaximumScore();
                }
            }
            if (max > 0) {
                System.out.printf("      Course result: %.1f%%%n", obtained * 100 / max);
            } else {
                System.out.println("      No assessments recorded yet.");
            }
            totalObtained += obtained;
            totalMax += max;
        }
        Registrable.printRow("Credits (not dropped)", credits);
        Registrable.printRow("Overall result", totalMax > 0 ? String.format("%.1f%%", totalObtained * 100 / totalMax) : "n/a");
        Registrable.printRow("Fees paid (RWF)", totalPaid(s).toPlainString());
        Registrable.printRow("Books on loan", loans.stream().filter(l -> l.getStudent() == s && l.isActive()).count());
    }

    // =================================================================== 17. Student payment history

    private static void studentPaymentHistory() {
        if (students.isEmpty()) {
            System.out.println("! No students are registered yet.");
            return;
        }
        Student s = selectFromList("STUDENT PAYMENT HISTORY", "student", students);
        if (s == null) {
            return;
        }
        Registrable.printHeading("PAYMENT HISTORY - " + s.getDisplayName());
        List<FeePayment> mine = payments.stream().filter(p -> p.getStudent() == s)
                .sorted(Comparator.comparing(FeePayment::getPaymentDate)).toList();
        if (mine.isEmpty()) {
            System.out.println("  No payments recorded for this student.");
            return;
        }
        System.out.printf("  %-10s %-12s %-14s %-15s %14s%n", "ID", "Date", "Receipt", "Method", "Amount (RWF)");
        for (FeePayment p : mine) {
            System.out.printf("  %-10s %-12s %-14s %-15s %14s%n", p.getId(), Registrable.formatDate(p.getPaymentDate()),
                    p.getReceiptNumber(), p.getPaymentMethod(), p.getAmount().toPlainString());
        }
        System.out.printf("  %-53s %14s%n", "TOTAL PAID (" + mine.size() + " payment(s))", totalPaid(s).toPlainString());
    }

    private static BigDecimal totalPaid(Student s) {
        return payments.stream().filter(p -> p.getStudent() == s).map(FeePayment::getAmount)
                .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    // =================================================================== 18. Available books

    private static void showAvailableBooks() {
        System.out.println("== Available library books ==");
        List<LibraryBook> available = availableBooks();
        if (available.isEmpty()) {
            System.out.println("   No books are available right now"
                    + (books.isEmpty() ? " (none registered yet)." : " (all copies are on loan)."));
            return;
        }
        available.stream().sorted(Comparator.comparing(LibraryBook::getTitle, String.CASE_INSENSITIVE_ORDER))
                .forEach(b -> System.out.println("   " + b.getRegistrationSummary()));
    }

    // =================================================================== 19. Polymorphism

    private static void demonstratePolymorphism() {
        System.out.println();
        System.out.println("=============== POLYMORPHISM DEMONSTRATION ===============");
        System.out.println("Hierarchy: Person -> AcademicMember -> Student | Teacher   (multilevel)");
        System.out.println("           Person -> Administrator                         (hierarchical)");
        System.out.println("Person and AcademicMember are ABSTRACT: 'new Person(...)' does not compile,");
        System.out.println("because they declare abstract methods (getRoleDescription, getAcademicCategory)");
        System.out.println("that only a concrete Student/Teacher/Administrator can implement.");

        List<Person> people = new ArrayList<>();
        people.addAll(students);
        people.addAll(teachers);
        people.addAll(administrators);
        System.out.println();
        System.out.println("1) List<Person> - overridden methods called through Person references:");
        if (people.isEmpty()) {
            System.out.println("   (register a student, teacher or administrator to see this part)");
        }
        for (Person p : people) {
            // Same call, different behaviour: decided at runtime by the real object type.
            System.out.printf("   %-13s %-10s %-24s role: %s%n", p.getClass().getSimpleName(), p.getId(),
                    p.getDisplayName(), p.getRoleDescription());
            if (p instanceof AcademicMember m) {
                System.out.println("                 academic category: " + m.getAcademicCategory());
            }
        }

        List<Identifiable> entities = new ArrayList<>();
        entities.addAll(departments);
        entities.addAll(courses);
        entities.addAll(classrooms);
        entities.addAll(enrollments);
        entities.addAll(assessments);
        entities.addAll(payments);
        entities.addAll(books);
        entities.addAll(loans);
        System.out.println();
        System.out.println("2) List<Identifiable> - unrelated classes used through one interface:");
        if (entities.isEmpty()) {
            System.out.println("   (register any non-person record to see this part)");
        }
        for (Identifiable e : entities) {
            System.out.printf("   %-12s %-10s %-45s %s%n", e.getClass().getSimpleName(), e.getId(),
                    e.getDisplayName(), e.isActive() ? "ACTIVE" : "INACTIVE");
        }

        if (!people.isEmpty()) {
            System.out.println();
            System.out.println("3) Registrable reference -> printDetails() (each level adds its part via super):");
            Registrable r = people.get(0);
            r.printDetails();
        }
    }

    // =================================================================== smart form engine

    /**
     * Fills a new record: runs the form, builds the object, validates it and re-checks
     * uniqueness. On a rule failure the user returns to the review screen with every
     * value kept. Returns null if the user cancels.
     */
    private static <T extends Registrable> T fillForm(String title, String noun, String[] steps, int[] fieldStep,
                                                      String[] labels, Object[] vals, boolean[] locked,
                                                      IntFunction<Object> reader, Function<Object[], T> builder,
                                                      boolean startAtReview) {
        boolean review = startAtReview;
        while (true) {
            if (!runForm(title, noun, steps, fieldStep, labels, vals, locked, reader, review)) {
                return null;
            }
            try {
                T record = builder.apply(vals);   // cross-record rules + duplicates
                record.validate();                 // every attribute rule
                return record;
            } catch (ValidationException | DuplicateRecordException e) {
                System.out.println("! Cannot save yet: " + e.getMessage());
                System.out.println("  Your values are kept. Use [2] Edit field to correct it.");
                review = true;
            }
        }
    }

    /**
     * Edits an existing record using the same form. Changes are applied only after
     * confirmation; if validation fails, the original values are restored.
     */
    private static void editRecord(String title, String noun, String[] steps, int[] fieldStep, String[] labels,
                                   Object[] vals, boolean[] locked, IntFunction<Object> reader,
                                   Consumer<Object[]> applier, Registrable target) {
        Object[] original = vals.clone();
        editingRecord = target;
        try {
            while (true) {
                if (!runForm(title, noun, steps, fieldStep, labels, vals, locked, reader, true)) {
                    System.out.println("Update cancelled. The record was not changed.");
                    return;
                }
                List<Integer> changed = new ArrayList<>();
                for (int i = 0; i < vals.length; i++) {
                    if (!Objects.equals(original[i], vals[i])) {
                        changed.add(i);
                    }
                }
                if (changed.isEmpty()) {
                    System.out.println("No changes were made.");
                    return;
                }
                System.out.println("Changes to apply:");
                for (int i : changed) {
                    System.out.printf("  %02d %-20s : %s  ->  %s%n", i + 1, labels[i], fmt(original[i]), fmt(vals[i]));
                }
                if (!confirm("Apply these changes?")) {
                    System.out.println("Update cancelled. The record was not changed.");
                    return;
                }
                try {
                    applier.accept(vals);
                    target.validate();
                    System.out.println("+ Record " + ((Identifiable) target).getId() + " updated successfully.");
                    return;
                } catch (ValidationException | DuplicateRecordException e) {
                    applier.accept(original);   // roll back to the last valid state
                    System.out.println("! Update rejected: " + e.getMessage());
                    System.out.println("  The original values were restored. Correct the field and save again.");
                }
            }
        } finally {
            editingRecord = null;
        }
    }

    /**
     * Walks the data steps, then shows the review screen.
     *
     * @return true when the user chose Save, false when the user confirmed Cancel.
     */
    private static boolean runForm(String title, String noun, String[] steps, int[] fieldStep, String[] labels,
                                   Object[] vals, boolean[] locked, IntFunction<Object> reader, boolean startAtReview) {
        int dataSteps = steps.length;
        int step = startAtReview ? dataSteps : 0;
        while (true) {
            if (step < dataSteps) {
                printFormHeader(title, noun, steps, step);
                System.out.println("[STEP " + (step + 1) + "/" + (dataSteps + 1) + "] " + steps[step].toUpperCase());
                boolean wentBack = false;
                for (int i = 0; i < labels.length; i++) {
                    if (fieldStep[i] != step) {
                        continue;
                    }
                    if (locked[i]) {
                        System.out.printf("%02d  %-20s : %s (cannot be changed)%n", i + 1, labels[i], fmt(vals[i]));
                        continue;
                    }
                    Object result = reader.apply(i);
                    if (result == CANCEL) {
                        if (confirmDiscard(noun)) {
                            return false;
                        }
                        i--;   // ask the same field again
                        continue;
                    }
                    if (result == BACK) {
                        if (step == 0) {
                            System.out.println("  Already at the first step.");
                            i--;
                            continue;
                        }
                        step--;
                        wentBack = true;
                        break;
                    }
                    vals[i] = result;
                }
                if (!wentBack) {
                    step++;
                }
            } else {
                printReview(noun, steps, fieldStep, labels, vals);
                String choice = readLine("Select option: ").toUpperCase();
                switch (choice) {
                    case "1" -> {
                        return true;
                    }
                    case "2" -> editField(labels, vals, locked, reader);
                    case "0", "CANCEL" -> {
                        if (confirmDiscard(noun)) {
                            return false;
                        }
                    }
                    case "BACK" -> step = dataSteps - 1;
                    default -> System.out.println("! Invalid selection: choose 1 Save, 2 Edit field or 0 Cancel.");
                }
            }
        }
    }

    private static void editField(String[] labels, Object[] vals, boolean[] locked, IntFunction<Object> reader) {
        int index = readInt("Field number to edit (1-" + labels.length + ", 0 to return): ", 0, labels.length) - 1;
        if (index < 0) {
            return;
        }
        if (locked[index]) {
            System.out.println("! Field " + String.format("%02d", index + 1) + " (" + labels[index]
                    + ") cannot be changed here.");
            return;
        }
        System.out.println("  Old value of field " + String.format("%02d", index + 1) + ": " + fmt(vals[index]));
        Object result = reader.apply(index);
        if (result == CANCEL || result == BACK) {
            System.out.println("  Edit abandoned; the old value is kept.");
            return;
        }
        vals[index] = result;
        System.out.println("  Field " + String.format("%02d", index + 1) + " updated.");
    }

    private static void printFormHeader(String title, String noun, String[] steps, int current) {
        System.out.println();
        System.out.println(boxBorder());
        System.out.println(boxLine("AUCA_ID  /  SCHOOL MANAGEMENT"));
        String stepText = "Step " + (current + 1) + " of " + (steps.length + 1);
        System.out.println(boxLine(padRight(title, BOX_WIDTH - 2 - stepText.length()) + stepText));
        System.out.println(boxBorder());
        String[] all = new String[steps.length + 1];
        for (int i = 0; i < steps.length; i++) {
            all[i] = "[" + (i + 1) + "] " + steps[i];
        }
        all[steps.length] = "[" + (steps.length + 1) + "] Review & confirm";
        for (int i = 0; i < all.length; i += 2) {
            String right = i + 1 < all.length ? all[i + 1] : "";
            System.out.println(boxLine(padRight(all[i], 30) + right));
        }
        System.out.println(boxBorder());
        System.out.println(boxLine("Enter the requested value. Type BACK to return;"));
        System.out.println(boxLine("CANCEL to discard the unsaved " + noun + "."));
        System.out.println(boxBorder());
    }

    private static void printReview(String noun, String[] steps, int[] fieldStep, String[] labels, Object[] vals) {
        System.out.println();
        System.out.println(boxTitle("REVIEW " + noun.toUpperCase()));
        for (int s = 0; s < steps.length; s++) {
            System.out.println(boxLine(steps[s].toUpperCase()));
            for (int i = 0; i < labels.length; i++) {
                if (fieldStep[i] == s) {
                    System.out.println(boxLine(String.format("%02d %-20s: %s", i + 1, labels[i], fmt(vals[i]))));
                }
            }
        }
        System.out.println(boxBorder());
        System.out.println(boxLine("[1] Save " + noun + "   [2] Edit field   [0] Cancel"));
        System.out.println(boxBorder());
    }

    // ------------------------------------------------------------------ field prompts

    /**
     * Asks one field until it is valid. ENTER keeps the current value (when going BACK or
     * editing); BACK and CANCEL are returned as sentinels. Every parsing problem is caught
     * here and only the affected field is asked again.
     */
    private static Object askField(int index, String label, String hint, Object current, Function<String, Object> parser) {
        String prompt = String.format("%02d  %-20s [%s]", index + 1, label, hint);
        if (current != null && current != NONE) {
            prompt += " (current: " + fmt(current) + ", ENTER keeps)";
        }
        prompt += " : ";
        while (true) {
            String raw = readLine(prompt);
            if (isCancel(raw)) {
                return CANCEL;
            }
            if (isBack(raw)) {
                return BACK;
            }
            if (raw.isEmpty() && current != null && current != NONE) {
                return current;
            }
            try {
                return parser.apply(raw);
            } catch (ValidationException | DuplicateRecordException e) {
                System.out.println("! " + e.getMessage());
            } catch (DateTimeParseException e) {
                System.out.println("! Invalid date: use YYYY-MM-DD (example: 2004-05-16) and a real calendar date.");
            } catch (NumberFormatException e) {
                System.out.println("! '" + raw + "' is not a valid number for " + label + ".");
            }
            prompt = String.format("  Please re-enter field %02d (%s): ", index + 1, label);
        }
    }

    /**
     * Numbered selection of an existing related object, with optional nested registration
     * ("[+] Register a new ...") and an optional "Leave unassigned" choice. The stored value
     * is the selected object itself, never the menu number.
     */
    private static Object askSelect(int index, String label, String entity, Supplier<List<? extends Identifiable>> options,
                                    Object current, Supplier<Identifiable> registerNew, boolean optional, String form) {
        while (true) {
            List<? extends Identifiable> list = options.get();
            System.out.printf("%02d  %-20s [%s | select]%n", index + 1, label, entity);
            System.out.println(boxTitle("CHOOSE " + entity.toUpperCase()));
            if (list.isEmpty()) {
                System.out.println(boxLine("No " + entity + " is registered yet."));
            }
            for (int k = 0; k < list.size(); k++) {
                Identifiable r = list.get(k);
                System.out.println(boxLine(String.format("%-3d [%s] %s", k + 1, r.getId(), r.getDisplayName())));
            }
            int next = list.size() + 1;
            int registerOption = -1;
            int noneOption = -1;
            if (registerNew != null) {
                registerOption = next++;
                System.out.println(boxLine(String.format("%-3d [+] Register a new %s", registerOption, entity)));
            }
            if (optional) {
                noneOption = next;
                System.out.println(boxLine(String.format("%-3d Leave unassigned", noneOption)));
            }
            System.out.println(boxLine("0   Cancel " + form.toLowerCase()));
            System.out.println(boxBorder());
            if (current != null) {
                System.out.println("  ENTER keeps current: " + fmt(current) + "    BACK = previous step");
            }
            String raw = readLine("Select option: ");
            if (raw.isEmpty() && current != null) {
                return current;
            }
            if (isBack(raw)) {
                return BACK;
            }
            if (isCancel(raw) || raw.equals("0")) {
                return CANCEL;
            }
            int choice;
            try {
                choice = Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                System.out.println("! '" + raw + "' is not a number. Choose one of the listed options.");
                continue;
            }
            if (choice >= 1 && choice <= list.size()) {
                return list.get(choice - 1);
            }
            if (choice == registerOption) {
                Identifiable created = nestedRegistration(entity, registerNew, form);
                if (created != null) {
                    if (options.get().contains(created)) {
                        return created;
                    }
                    System.out.println("! The new " + entity + " cannot be used here (for example it has no available "
                            + "copies). Choose another option.");
                }
                continue;
            }
            if (choice == noneOption) {
                return NONE;
            }
            System.out.println("! " + choice + " is not one of the listed options.");
        }
    }

    /** Opens another registration form inside the current one; the outer form keeps its values. */
    private static Identifiable nestedRegistration(String entity, Supplier<Identifiable> register, String form) {
        Object outerEditing = editingRecord;
        editingRecord = null;
        try {
            System.out.println(">> Opening " + entity + " registration. Your " + form.toLowerCase()
                    + " values are kept.");
            Identifiable created = register.get();
            if (created == null) {
                System.out.println("<< " + entity + " registration cancelled. Back to " + form.toLowerCase() + ".");
            }
            return created;
        } finally {
            editingRecord = outerEditing;
        }
    }

    /**
     * Checks that a required related record exists before a form starts. If not, explains why
     * and offers: 1 Register it now (then resume) / 2 Return to menu.
     */
    private static boolean ensurePrerequisite(String entity, String purpose, Supplier<List<? extends Identifiable>> list,
                                              Supplier<Identifiable> register, String form) {
        while (true) {
            try {
                requirePrerequisite(list.get(), entity, purpose);
                return true;
            } catch (PrerequisiteMissingException e) {
                System.out.println("! " + e.getMessage());
                System.out.println("  1 Register " + entity + " now   2 Return to menu");
                if (readInt("Select: ", 0, 2) != 1) {
                    return false;
                }
                nestedRegistration(entity, register, form);
            }
        }
    }

    private static void requirePrerequisite(List<?> records, String entity, String purpose)
            throws PrerequisiteMissingException {
        if (records.isEmpty()) {
            throw new PrerequisiteMissingException("No " + entity + " exists. Register a " + entity.toLowerCase()
                    + " before " + purpose + ".");
        }
    }

    /** Simple numbered picker used outside the forms (summaries, returns). */
    private static <T extends Identifiable> T selectFromList(String title, String entity, List<T> items) {
        while (true) {
            System.out.println(boxTitle(title));
            for (int k = 0; k < items.size(); k++) {
                System.out.println(boxLine(String.format("%-3d [%s] %s", k + 1, items.get(k).getId(),
                        items.get(k).getDisplayName())));
            }
            System.out.println(boxLine("0   Cancel"));
            System.out.println(boxBorder());
            int choice = readInt("Select " + entity + ": ", 0, items.size());
            return choice == 0 ? null : items.get(choice - 1);
        }
    }

    private static void announceSaved(String entity, Identifiable record, String returnTo) {
        System.out.println("+ " + entity + " " + record.getId() + " - " + record.getDisplayName()
                + " registered successfully.");
        if (returnTo != null) {
            System.out.println("  " + entity + " saved. Returning to " + returnTo + "...");
        }
    }

    // ------------------------------------------------------------------ parsing helpers

    private static String uniqueId(String raw, String label) {
        String id = Identifiable.validateId(raw, label);
        return requireUnique(id, idExists(id), label);
    }

    private static String requireUnique(String value, boolean exists, String label) {
        if (exists) {
            throw new DuplicateRecordException(label + " " + value + " already exists. Please enter a different value.");
        }
        return value;
    }

    private static boolean idExists(String id) {
        return allRecords().stream().anyMatch(r -> r != editingRecord && r.getId().equalsIgnoreCase(id));
    }

    private static LocalDate notFutureNotBefore(String raw, String label, LocalDate earliest, String earliestLabel) {
        LocalDate date = Registrable.requireNotFuture(Registrable.parseDate(raw, label), label);
        return Registrable.requireNotBefore(date, earliest, label, earliestLabel);
    }

    private static int intInRange(String raw, String label, int min, int max) {
        return Registrable.requireRange(Integer.parseInt(Registrable.requireText(raw, label)), min, max, label);
    }

    private static double parseScore(String raw, String label) {
        double score = Double.parseDouble(Registrable.requireText(raw, label));
        if (!Double.isFinite(score)) {
            throw new ValidationException(label + " must be a normal number.");
        }
        return score;
    }

    private static Boolean parseYesNo(String raw, String label) {
        String text = Registrable.requireText(raw, label).toUpperCase();
        if (text.equals("1") || text.equals("YES") || text.equals("Y")) {
            return Boolean.TRUE;
        }
        if (text.equals("2") || text.equals("NO") || text.equals("N")) {
            return Boolean.FALSE;
        }
        throw new ValidationException(label + " must be 1 (YES) or 2 (NO).");
    }

    /** Accepts the option number or the value itself (e.g. "2" or "INACTIVE"). */
    private static String pickChoice(String raw, String[] allowed, String label) {
        String text = Registrable.requireText(raw, label).toUpperCase().replace(' ', '_');
        if (text.matches("\\d+")) {
            int n = Integer.parseInt(text);
            if (n >= 1 && n <= allowed.length) {
                return allowed[n - 1];
            }
        }
        return Registrable.requireAllowed(text, label + " (" + choiceHint(allowed) + ")", allowed);
    }

    // ------------------------------------------------------------------ console input

    /** The only place input is read: one shared Scanner, always nextLine(). */
    private static String readLine(String prompt) {
        System.out.print(prompt);
        if (!SCANNER.hasNextLine()) {
            System.out.println();
            System.out.println("Input stream closed. Exiting AUCA_ID.");
            System.exit(0);
        }
        return SCANNER.nextLine().trim();
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            String raw = readLine(prompt);
            try {
                int value = Integer.parseInt(raw);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("! " + value + " is out of range. Enter a number from " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("! '" + raw + "' is not a number. Enter a number from " + min + " to " + max + ".");
            }
        }
    }

    private static boolean confirm(String question) {
        while (true) {
            String raw = readLine(question + "  1 Yes  2 No : ").toUpperCase();
            if (raw.equals("1") || raw.equals("YES") || raw.equals("Y")) {
                return true;
            }
            if (raw.equals("2") || raw.equals("NO") || raw.equals("N")) {
                return false;
            }
            System.out.println("! Please answer 1 (Yes) or 2 (No).");
        }
    }

    private static boolean confirmDiscard(String noun) {
        return confirm("Discard the unsaved " + noun + "? Nothing will be saved.");
    }

    private static boolean isCancel(String raw) {
        return raw.equalsIgnoreCase("CANCEL");
    }

    private static boolean isBack(String raw) {
        return raw.equalsIgnoreCase("BACK");
    }

    // ------------------------------------------------------------------ collections

    private static List<Identifiable> allRecords() {
        List<Identifiable> all = new ArrayList<>();
        all.addAll(departments);
        all.addAll(students);
        all.addAll(teachers);
        all.addAll(administrators);
        all.addAll(courses);
        all.addAll(classrooms);
        all.addAll(enrollments);
        all.addAll(assessments);
        all.addAll(payments);
        all.addAll(books);
        all.addAll(loans);
        return all;
    }

    private static List<Person> allPersons() {
        List<Person> all = new ArrayList<>(students);
        all.addAll(teachers);
        all.addAll(administrators);
        return all;
    }

    private static List<AcademicMember> academicMembers() {
        List<AcademicMember> all = new ArrayList<>(students);
        all.addAll(teachers);
        return all;
    }

    private static List<Enrollment> activeEnrollments() {
        return enrollments.stream().filter(Enrollment::isActive).toList();
    }

    private static List<LibraryBook> availableBooks() {
        return books.stream().filter(b -> b.getAvailableCopies() > 0).toList();
    }

    // ------------------------------------------------------------------ formatting / arrays

    private static String fmt(Object value) {
        if (value == null) {
            return "(not entered)";
        }
        if (value == NONE) {
            return "(none)";
        }
        if (value instanceof LocalDate d) {
            return Registrable.formatDate(d);
        }
        if (value instanceof BigDecimal b) {
            return b.toPlainString();
        }
        if (value instanceof Boolean b) {
            return b ? "YES" : "NO";
        }
        if (value instanceof Identifiable r) {
            return "[" + r.getId() + "] " + r.getDisplayName();
        }
        return value.toString();
    }

    @SuppressWarnings("unchecked")
    private static <T> T val(Object value) {
        return value == NONE ? null : (T) value;
    }

    private static String choiceHint(String[] allowed) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < allowed.length; i++) {
            sb.append(i == 0 ? "" : " / ").append(i + 1).append(' ').append(allowed[i]);
        }
        return sb.toString();
    }

    private static String[] concat(String[]... parts) {
        List<String> all = new ArrayList<>();
        for (String[] part : parts) {
            all.addAll(List.of(part));
        }
        return all.toArray(new String[0]);
    }

    private static Object[] concatValues(Object[]... parts) {
        List<Object> all = new ArrayList<>();
        for (Object[] part : parts) {
            all.addAll(java.util.Arrays.asList(part));
        }
        return all.toArray();
    }

    /** stepMap(6, 6, 6) -> the first 6 fields are step 0, the next 6 step 1, the last 6 step 2. */
    private static int[] stepMap(int... counts) {
        int total = 0;
        for (int c : counts) {
            total += c;
        }
        int[] map = new int[total];
        int k = 0;
        for (int step = 0; step < counts.length; step++) {
            for (int j = 0; j < counts[step]; j++) {
                map[k++] = step;
            }
        }
        return map;
    }

    private static boolean[] lockFirst(int size) {
        boolean[] locked = new boolean[size];
        locked[0] = true;   // the primary ID can never be changed
        return locked;
    }

    private static String boxBorder() {
        return "+" + "-".repeat(BOX_WIDTH) + "+";
    }

    private static String boxLine(String content) {
        return "| " + padRight(content, BOX_WIDTH - 2) + " |";
    }

    private static String boxTitle(String title) {
        String text = " " + title + " ";
        int left = Math.max(1, (BOX_WIDTH - text.length()) / 2);
        int right = Math.max(1, BOX_WIDTH - text.length() - left);
        return "+" + "-".repeat(left) + text + "-".repeat(right) + "+";
    }

    private static String padRight(String text, int width) {
        return text.length() >= width ? text : text + " ".repeat(width - text.length());
    }
}
