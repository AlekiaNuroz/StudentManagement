import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Manages student-related operations including CRUD actions, enrollment management, and data persistence.
 * <p>
 * This class serves as the primary interface between student data, user input (via console), and database operations.
 * Key responsibilities include:
 * <ul>
 *   <li>Maintaining an in-memory list of active students synchronized with the database</li>
 *   <li>Handling user input through console interactions</li>
 *   <li>Coordinating database operations through {@link DatabaseManager}</li>
 * </ul>
 *
 * @see DatabaseManager
 * @see Student
 */
public class StudentManagement {
    private final List<Student> students;
    private final Scanner scanner;
    private final DatabaseManager db;

    /**
     * Initializes a new StudentManagement instance with database-backed student data.
     * <p>
     * During construction:
     * <ul>
     *   <li>Loads active students (non-deleted) from the database</li>
     *   <li>Stores references to database manager and input scanner</li>
     * </ul>
     *
     * @param db The database manager responsible for persistence operations
     * @param scanner The input scanner for handling user interactions
     *
     * @implNote The students list is initialized with records where deleted=false via {@link DatabaseManager#getStudents(boolean)}.
     *           Maintains a single Scanner instance for consistent input handling throughout the application lifecycle.
     */
    public StudentManagement(DatabaseManager db, Scanner scanner) {
        this.students = db.getStudents(false);
        this.scanner = scanner;
        this.db = db;
    }

    /**
     * Interactively adds a new student to both the in-memory list and database after validation.
     * <p>
     * This method performs the following operations:
     * <ol>
     *   <li>Clears the screen using {@link IOHelper#clearScreen()}</li>
     *   <li>Prompts for a student ID and checks for existing records via {@link DatabaseManager#getStudent(String)}</li>
     *   <li>If the student exists:
     *     <ul>
     *       <li>Displays error message and aborts operation</li>
     *     </ul>
     *   </li>
     *   <li>If new:
     *     <ul>
     *       <li>Collects student name via {@link #createStudent(String)} helper</li>
     *       <li>Persists to database via {@link DatabaseManager#insertStudent(Student)}</li>
     *       <li>On success: adds to in-memory list and displays confirmation</li>
     *       <li>On failure: displays error message</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * @implNote Maintains consistency between database and in-memory data.
     *           Student ID validation is case-sensitive. Requires proper transaction handling
     *           in {@link DatabaseManager} methods to ensure data integrity.
     */
    public void addStudent() {
        IOHelper.clearScreen();

        String StudentId = IOHelper.getStringInput(scanner, "Enter a student ID: ", false);

        if (db.getStudent(StudentId) != null) {
            System.out.println("Student already exists in the database.");
            return;
        }

        Student newStudent = createStudent(StudentId);

        if (db.insertStudent(newStudent)) {
            students.add(newStudent);
            System.out.println("Student added successfully.");
        } else {
            System.out.println("Failed to add student.");
        }
    }

    /**
     * Creates a new {@link Student} instance by prompting for and validating required fields.
     * <p>
     * This method:
     * <ul>
     *   <li>Collects the student's name interactively via {@link IOHelper#getStringInput}</li>
     *   <li>Ensures non-blank name input (enforced by passing {@code false} to IOHelper)</li>
     *   <li>Constructs a {@link Student} with the provided ID and collected name</li>
     * </ul>
     *
     * @param studentId The pre-determined student ID to assign (typically generated/validated externally)
     * @return A fully initialized {@link Student} object with guaranteed non-null name
     *
     * @implNote This is a helper method intended for centralized student creation during add/restore workflows.
     *           Does NOT handle database persistence - caller must manage storage operations.
     */
    private Student createStudent(String studentId) {
        String name = IOHelper.getStringInput(scanner, "Enter the student's name: ", false);
        return new Student(studentId, name);
    }

    /**
     * Soft-deletes a student by moving them from the active list to a deleted state in the database.
     * <p>
     * This method performs the following operations:
     * <ol>
     *   <li>Clears the screen via {@link IOHelper#clearScreen()}</li>
     *   <li>Displays current students using {@link #listStudents()}</li>
     *   <li>Prompts for a student ID to delete</li>
     *   <li>Searches for the student in the active list:
     *     <ul>
     *       <li>If found:
     *         <ul>
     *           <li>Removes the student from the in-memory {@code students} collection</li>
     *           <li>Persists the deletion via {@link DatabaseManager#deleteRestoreStudent(Student, boolean)} with {@code true}</li>
     *           <li>Displays success message</li>
     *         </ul>
     *       </li>
     *       <li>If not found, displays error message and pauses briefly via {@link IOHelper#wait(int)}</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * @implNote This implements a soft-delete pattern - the student is marked as deleted in persistence
     *           but not permanently erased. Uses linear search (O(n) time) via stream operations.
     *           Maintains synchronization between in-memory state and database.
     */
    public void removeStudent() {
        IOHelper.clearScreen();
        listStudents();

        String id = IOHelper.getStringInput(scanner, "\nEnter a student id you wish to delete: ", false);

        students.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .ifPresentOrElse(student -> {
                    students.remove(student);
                    db.deleteRestoreStudent(student, true);
                    System.out.println("Student removed successfully.");
                }, () -> {
                    System.out.println("Student does not exist.");
                    IOHelper.wait(1);
                });
    }

    /**
     * Restores a previously deleted student from the database and adds them back to the active student list.
     * <p>
     * This method:
     * <ol>
     *   <li>Clears the screen via {@link IOHelper#clearScreen()}</li>
     *   <li>Retrieves all soft-deleted students using {@link DatabaseManager#getStudents(boolean)} with {@code true}</li>
     *   <li>Displays the list of deleted students</li>
     *   <li>Prompts for a student identifier (though the prompt text says "course code")</li>
     *   <li>Searches for a matching deleted student</li>
     *   <li>If found:
     *     <ul>
     *       <li>Adds the student back to the active {@code students} collection</li>
     *       <li>Updates persistence layer via {@link DatabaseManager#deleteRestoreStudent(Student, boolean)} with {@code false}</li>
     *     </ul>
     *   </li>
     *   <li>If not found, displays an error message</li>
     * </ol>
     *
     * @implNote The restoration process affects both in-memory state and database persistence.
     *           Requires proper transaction handling in {@link DatabaseManager} methods.
     */
    public void restoreStudent() {
        IOHelper.clearScreen();
        List<Student> deletedStudents = db.getStudents(true);

        System.out.println("Deleted students:");
        deletedStudents.forEach(Student -> System.out.println("\t" + Student.getId() + " - " + Student.getName()));

        String studentId = IOHelper.getStringInput(scanner, "Enter a student id to restore: ", false);

        deletedStudents.stream()
                .filter(c -> c.getId().equals(studentId))
                .findFirst()
                .ifPresentOrElse(student -> {
                    students.add(student);
                    db.deleteRestoreStudent(student, false);
                    System.out.println("Student restored successfully.");
                    IOHelper.wait(1);
                }, () -> System.out.println("Student not found."));
    }

    /**
     * Displays all students in a formatted table view with dynamically adjusted column widths.
     * <p>
     * The method performs the following operations:
     * <ol>
     *   <li>Clears the console using {@link IOHelper#clearScreen()}</li>
     *   <li>Calculates column widths based on:
     *     <ul>
     *       <li>The longest student ID and name in the list</li>
     *       <li>Header text ("Student ID" and "Student Name")</li>
     *     </ul>
     *   </li>
     *   <li>Prints a table with:
     *     <ul>
     *       <li>Header row with column titles</li>
     *       <li>A separator line of hyphens</li>
     *       <li>Rows for each student with left-aligned ID and name</li>
     *     </ul>
     *   </li>
     *   <li>Displays "No students found" if the list is empty</li>
     * </ol>
     *
     * @implNote The table formatting uses printf with dynamic width specifiers.
     *           Column widths are determined via a linear scan of all students (O(n) time).
     *           Console output assumes monospaced font for proper alignment.
     */
    public void listStudents() {
        IOHelper.clearScreen();

        int maxIdWidth = 0;
        int maxNameWidth = 0;

        if (!students.isEmpty()) {
            for (Student student : students) {
                maxIdWidth = Math.max(maxIdWidth, student.getId().length());
                maxNameWidth = Math.max(maxNameWidth, student.getName().length());
            }

            maxIdWidth = Math.max("Student ID".length(), maxIdWidth);

            // Format String
            String format = "| %-" + maxIdWidth + "s | %-" + maxNameWidth + "s |\n";

            // Print Header
            System.out.printf(format, "Student ID", "Student Name");
            System.out.println("|" + "-".repeat(maxIdWidth + 2) + "|" + "-".repeat(maxNameWidth + 2) + "|");

            // Print Students
            for (Student student : students) {
                System.out.printf(format, student.getId(), student.getName());
            }
        } else {
            System.out.println("No students found");
        }
    }

    /**
     * Searches for a student by their ID and returns the result as an {@link Optional}.
     * <p>
     * This method performs a case-sensitive search through the internal student list and returns:
     * <ul>
     *   <li>An {@link Optional} containing the first {@link Student} with a matching ID, if found.</li>
     *   <li>{@link Optional#empty()} if no student matches the ID.</li>
     * </ul>
     *
     * @param studentId The student ID to search for (exact case-sensitive match required)
     * @return A non-null {@link Optional} wrapping the matched student or empty if no match exists
     */
    public Optional<Student> findStudentById(String studentId) {
        return students.stream().filter(s -> s.getId().equals(studentId)).findFirst();
    }

    /**
     * Enrolls a student in a selected course based on user input, handling both database persistence and in-memory updates.
     * <p>
     * This method follows these steps:
     * <ol>
     *   <li>Displays all students by calling {@link #listStudents()}.</li>
     *   <li>Prompts the user to enter a student ID for enrollment.</li>
     *   <li>Displays available courses using {@link CourseManagement#listCourses()}.</li>
     *   <li>Prompts the user to enter a course ID for enrollment.</li>
     *   <li>Retrieves the {@link Course} object from the database.</li>
     *   <li>Checks if the student exists and validates enrollment status:
     *     <ul>
     *       <li>If already enrolled, displays an error message and exits.</li>
     *       <li>If not enrolled, persists the enrollment to the database via {@link DatabaseManager#enrollStudentInCourse(String, String)}.</li>
     *       <li>Updates the in-memory {@link Student} object on successful database enrollment.</li>
     *     </ul>
     *   </li>
     *   <li>Displays success/failure messages with brief pauses using {@link IOHelper#wait(int)}.</li>
     * </ol>
     *
     * @param courseManagement The {@link CourseManagement} instance used to list available courses.
     *
     * @implNote This method ensures synchronization between the database and in-memory data.
     *           Console interactions are managed via {@link IOHelper} utilities.
     */
    public void enrollStudentInCourse(CourseManagement courseManagement) {
        listStudents();

        String studentId = IOHelper.getStringInput(scanner, "\nEnter a student id to enroll: ", false);

        courseManagement.listCourses();

        String courseId = IOHelper.getStringInput(scanner, "\nEnter a course code to enroll: ", false);

        Course courseToEnroll = db.getCourse(courseId);

        Optional<Student> studentOpt = findStudentById(studentId);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            // Check if already enrolled in memory
            if (student.getEnrolledCourses().containsKey(courseToEnroll)) {
                System.out.println(student.getName() + " is already enrolled in " + courseToEnroll.getName());
                IOHelper.wait(1);
                return;
            }
            // Persist to database
            if (db.enrollStudentInCourse(studentId, courseToEnroll.getId())) {

                student.enrollInCourse(courseToEnroll);
                System.out.println(student.getName() + " successfully enrolled in " + courseToEnroll.getName());
                IOHelper.wait(1);
            } else {
                System.out.println("Failed to enroll student in course.");
            }
        } else {
            System.out.println("Student not found.");
        }
    }

    /**
     * Lists the enrolled courses and grades for a specific student based on user input.
     * <p>
     * This method performs the following steps:
     * <ol>
     *   <li>Displays a list of all students by calling {@link #listStudents()}.</li>
     *   <li>Prompts the user to enter a student ID via the console.</li>
     *   <li>Searches for the student using the provided ID.</li>
     *   <li>If the student is found:
     *     <ul>
     *       <li>Clears the console screen using {@link IOHelper#clearScreen()}.</li>
     *       <li>Displays the student's ID, name, and enrolled courses with grades (or "No grade" if ungraded).</li>
     *       <li>Waits for the user to press ENTER to continue.</li>
     *     </ul>
     *   </li>
     *   <li>If the student is not found, displays an error message and pauses briefly.</li>
     * </ol>
     *
     * @implNote This method interacts with the console for input/output operations and
     *           relies on {@link IOHelper} utilities for input handling and screen management.
     */
    public void listStudentEnrollments() {
        listStudents();

        String studentId = IOHelper.getStringInput(scanner, "\nEnter a student id: ", false);

        Optional<Student> studentOpt = findStudentById(studentId);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            IOHelper.clearScreen();
            System.out.println("Student ID: " + student.getId());
            System.out.println("Student Name: " + student.getName());
            System.out.println("Enrolled Courses:");
            student.getEnrolledCourses().forEach( (course, grade) -> System.out.println("\t" + course.getId() + " - "
                    + course.getName() + ": " + ((grade == null)? "No grade" : grade + "%")));
            IOHelper.getStringInput(scanner, "\nPress ENTER to continue", true);
        } else {
            System.out.println("Student not found.");
            IOHelper.wait(1);
        }
    }

    /**
     * Interactively assigns a grade to a student's course enrollment through console input.
     * <p>
     * This method guides the user through:
     * <ol>
     *   <li>Listing all students via {@link #listStudents()}</li>
     *   <li>Selecting a student by ID</li>
     *   <li>Displaying the student's enrolled courses</li>
     *   <li>Selecting a course and validating grade input (0.0-100.0)</li>
     *   <li>Persisting the grade via {@link DatabaseManager#assignGrade(String, String, Double)}</li>
     *   <li>Updating in-memory student data on success</li>
     * </ol>
     *
     * @implNote
     * <ul>
     *   <li>Does not validate course enrollment status before grading - relies on database enforcement</li>
     *   <li>Grade input is validated via {@link IOHelper#getDoubleInput}</li>
     *   <li>Silently fails if course doesn't exist (courseToGrade could be null)</li>
     *   <li>Maintains sync between database and in-memory {@link Student} object</li>
     *   <li>Uses 1-second pauses via {@link IOHelper#wait(int)} for message visibility</li>
     * </ul>
     */
    @SuppressWarnings("unused")
    public void assignGradeToStudent() {
        listStudents();

        String studentId = IOHelper.getStringInput(scanner, "\nEnter a student id: ", false);

        Optional<Student> studentOpt = findStudentById(studentId);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();

            System.out.println(student.getName() +"'s current enrollments:");
            student.getEnrolledCourses().forEach( (course, grade) -> System.out.println("\t" + course.getId() + " - "
                    + course.getName()));

            String courseId = IOHelper.getStringInput(scanner, "\nEnter a course code: ", false);
            Course courseToGrade = db.getCourse(courseId);

            double gradeToAssign = IOHelper.getDoubleInput(scanner, "Enter a grade to assign: ", 0.0, 100.0, false, 0.0);

            if (db.assignGrade(studentId, courseId, gradeToAssign)) {
                student.assignGrade(courseToGrade, gradeToAssign);
                System.out.println(student.getName() + " successfully graded in " + courseToGrade.getName());
                IOHelper.wait(1);
            }
        } else {
            System.out.println("Student not found.");
            IOHelper.wait(1);
        }
    }

    /**
     * Interactively updates a student's name through console input and database persistence.
     * <p>
     * This method:
     * <ol>
     *   <li>Lists all students via {@link #listStudents()}</li>
     *   <li>Prompts for a student ID (case-sensitive match)</li>
     *   <li>Validates student existence via {@link #findStudentById(String)}</li>
     *   <li>Collects new name input with validation</li>
     *   <li>Updates both in-memory {@link Student} object and database record</li>
     * </ol>
     *
     * @implNote
     * <ul>
     *   <li>Silently fails if student ID isn't found (no error message displayed)</li>
     *   <li>Name validation (non-blank) is enforced by {@link IOHelper}</li>
     *   <li>In-memory update occurs before database confirmation - could create temporary inconsistency</li>
     *   <li>Success message depends on database operation, not just in-memory update</li>
     * </ul>
     */
    public void updateStudentName() {
        listStudents();

        String studentId = IOHelper.getStringInput(scanner, "\nEnter a student id: ", false);
        Optional<Student> studentOpt = findStudentById(studentId);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();

            String newStudentName = IOHelper.getStringInput(scanner, "\nEnter a new name for the student: ", false);

            student.setName(newStudentName);

            if (db.updateStudentName(studentId, newStudentName)) {
                System.out.println("Student name successfully updated.");
                IOHelper.wait(1);
            }
        }
    }
}