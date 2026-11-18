# AUCA_ID – School Management System (console, Java OOP)

## Run it

Requires JDK 17 or newer.

**Terminal (from inside the `AUCA_ID` folder):**

```bash
# Windows (PowerShell)
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out com.auca.main.Main

# macOS / Linux
javac -d out $(find src -name "*.java")
java -cp out com.auca.main.Main
```

**IntelliJ IDEA:** File → Open → `AUCA_ID`. Right-click `src` → *Mark Directory as* → *Sources Root*.
Run `com.auca.main.Main`.
**Eclipse / NetBeans:** create a Java project on this folder with `src` as the source folder, then run `Main`.

## Package tree

```
AUCA_ID/src/com/auca/
├── interfaces/     Identifiable, Registrable
├── abstracts/      Person, AcademicMember
├── student/        Student            (Person -> AcademicMember -> Student)
├── teacher/        Teacher            (Person -> AcademicMember -> Teacher)
├── administrator/  Administrator      (Person -> Administrator)
├── department/     Department
├── course/         Course
├── classroom/      Classroom
├── enrollment/     Enrollment
├── assessment/     Assessment
├── feepayment/     FeePayment
├── librarybook/    LibraryBook
├── bookloan/       BookLoan
├── exceptions/     ValidationException, DuplicateRecordException, RecordNotFoundException,
│                   PrerequisiteMissingException, BookUnavailableException
└── main/           Main
```

The project has exactly 15 domain types (2 interfaces, 2 abstract classes and 11 concrete classes), plus `Main` and the 5 exceptions.
Each domain class declares exactly its six prescribed attributes, using the names and types from the brief.

## Input conventions in every form

| Type | Meaning |
|---|---|
| `BACK` | go to the previous step (values you typed are kept; ENTER keeps a value) |
| `CANCEL` (or `0` in a selection list) | discard the unsaved record, after confirmation |
| `1`/`2` or the word | choose from a list, e.g. `1` or `ACTIVE`; `1` YES / `2` NO |
| Dates | `YYYY-MM-DD` (real calendar dates only: 2025-02-30 is rejected) |
| Semester | `FALL 2026`, `SPRING 2027`, `SUMMER 2027` |

See `DEMO_GUIDE.md` for a step-by-step demonstration script and answers to likely examiner questions.
