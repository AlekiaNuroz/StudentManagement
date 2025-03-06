import java.util.Scanner;

/**
 * Entry point class for the Student Management System application.
 * <p>
 * This class coordinates the application lifecycle by:
 * <ol>
 *   <li>Initializing core components (database connection, input scanner)</li>
 *   <li>Creating management system instances</li>
 *   <li>Running the main application loop</li>
 *   <li>Handling clean resource shutdown</li>
 * </ol>
 *
 * @see DatabaseManager
 * @see CourseManagement
 */
public class Main {

    /**
     * Main entry point that initializes and runs the application.
     * <p>
     * Execution flow:
     * <ol>
     *   <li>Creates input scanner for user interactions</li>
     *   <li>Initializes database connection manager</li>
     *   <li>Sets up course management system</li>
     *   <li>Enters main loop controlled by {@link #handleMainMenu}</li>
     *   <li>Closes resources on exit</li>
     * </ol>
     *
     * @param args Command-line arguments (not currently used)
     *
     * @implNote
     * <ul>
     *   <li>Uses a single Scanner instance for system-wide input</li>
     *   <li>Maintains application state until explicit exit command</li>
     *   <li>Does NOT handle unexpected exceptions - may crash on critical errors</li>
     *   <li>Scanner closure may trigger {@link IllegalStateException} if reused</li>
     * </ul>
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        DatabaseManager databaseManager = new DatabaseManager();
        CourseManagement courseManagement = new CourseManagement(databaseManager, scanner);
        boolean running = true;

        while (running) {
            running = handleMainMenu(databaseManager, courseManagement, scanner);
        }

        scanner.close();
    }

    /**
     * Handles navigation for the main menu of the Student Management System.
     * <p>
     * This method:
     * <ol>
     *   <li>Displays the main menu with options to manage courses, manage students, or exit</li>
     *   <li>Validates user input within the range 1-3 using {@link IOHelper}</li>
     *   <li>Delegates to subsystem menus based on user selection</li>
     *   <li>Controls program termination state through return value</li>
     * </ol>
     *
     * @param databaseManager   Database connection manager for persistence operations
     * @param courseManagement  Course system instance for dependency injection
     * @param scanner           Input scanner for user interactions
     * @return {@code true} to keep the program running, {@code false} to exit
     *
     * @implNote
     * <ul>
     *   <li>Menu displays 2 primary options but includes a 3rd hidden exit option</li>
     *   <li>Input validation ensures only 1-3 are accepted (enforced by {@link IOHelper#getIntInput})</li>
     *   <li>The return value drives the main program loop's continuation state</li>
     *   <li>Subsystem menus ({@link #manageCourses} and {@link #manageStudents}) handle their own session loops</li>
     * </ul>
     */
    private static boolean handleMainMenu(DatabaseManager databaseManager, CourseManagement courseManagement, Scanner scanner) {
        IOHelper.clearScreen();
        IOHelper.printMenu("Welcome to the Student Management System",
                new String[]{"Manage Courses", "Manage Students"});

        int choice = IOHelper.getIntInput(scanner, "Enter choice: ", 1, 3);
        switch (choice) {
            case 1:
                manageCourses(databaseManager, scanner);
                break;
            case 2:
                manageStudents(databaseManager, courseManagement, scanner);
                break;
            case 3:
                return false; // Exit the program
            default:
                System.out.println("Invalid choice. Try again.");
                IOHelper.wait(1);
        }
        return true;
    }

    /**
     * Manages the course administration interface through a console-based menu system.
     * <p>
     * This method:
     * <ol>
     *   <li>Initializes course management components</li>
     *   <li>Displays an interactive menu with course-related operations</li>
     *   <li>Handles user input through numbered options (1-8)</li>
     *   <li>Delegates execution to {@link #handleCourseChoice} for option processing</li>
     *   <li>Maintains session until user exits via menu choice</li>
     * </ol>
     *
     * @param db       Database connection manager for persistence operations
     * @param scanner  Input scanner for user interactions
     *
     * @implNote
     * <ul>
     *   <li>Menu displays 7 options but accepts input range 1-8 - likely uses 8 as exit code</li>
     *   <li>"Calculate Overall Grade" option may belong to student management (potential misplacement)</li>
     *   <li>Uses {@link IOHelper} for screen management and input validation</li>
     *   <li>Menu option numbers must align with {@code handleCourseChoice} implementation</li>
     *   <li>Creates new {@link CourseManagement} instance for session isolation</li>
     * </ul>
     */
    private static void manageCourses(DatabaseManager db, Scanner scanner) {
        CourseManagement courseManagement = new CourseManagement(db, scanner);
        boolean running = true;

        while (running) {
            IOHelper.clearScreen();
            IOHelper.printMenu("Manage Courses",
                    new String[]{"List Courses", "Add Course", "Delete Course", "Restore Course", "Calculate Overall Grade",
                            "Update Course Name", "Update Course Maximum Capacity"});

            int choice = IOHelper.getIntInput(scanner, "Enter choice: ", 1, 8);
            running = handleCourseChoice(scanner, courseManagement, choice);
        }
    }

    /**
     * Manages the student administration interface through a console-based menu system.
     * <p>
     * This method:
     * <ol>
     *   <li>Initializes student management components</li>
     *   <li>Displays an interactive menu with student-related operations</li>
     *   <li>Handles user input through numbered options (1-8)</li>
     *   <li>Delegates execution to {@link #handleStudentChoice} for option processing</li>
     *   <li>Maintains session until user exits via menu choice</li>
     * </ol>
     *
     * @param db              Database connection manager for persistence operations
     * @param courseManagement Course system instance for cross-functional operations
     * @param scanner         Input scanner for user interaction
     *
     * @implNote
     * <ul>
     *   <li>Menu text shows "Manage Courses" header but contains student operations (potential typo)</li>
     *   <li>Uses {@link IOHelper} for screen management and input validation</li>
     *   <li>Menu option numbers (1-9) must align with {@code handleStudentChoice} implementation</li>
     *   <li>Creates new {@link StudentManagement} instance for session isolation</li>
     * </ul>
     */
    private static void manageStudents(DatabaseManager db, CourseManagement courseManagement, Scanner scanner) {
        StudentManagement studentManagement = new StudentManagement(db, scanner);
        boolean running = true;

        while (running) {
            IOHelper.clearScreen();
            IOHelper.printMenu("Manage Courses",
                    new String[]{"List Students", "Add Student", "Delete Student", "Restore Student", "Enroll Student",
                            "List Enrollments", "Add Grade", "Update Student Name"});

            int choice = IOHelper.getIntInput(scanner, "Enter choice: ", 1, 9);
            running = handleStudentChoice(scanner, courseManagement, studentManagement, choice);
        }
    }

    /**
     * Routes student management menu selections to appropriate operations.
     * <p>
     * This method acts as a dispatcher for student-related actions based on user input:
     * <ol>
     *   <li>1: List students with pause</li>
     *   <li>2: Add new student</li>
     *   <li>3: Delete student</li>
     *   <li>4: Restore deleted student</li>
     *   <li>5: Enroll student in course</li>
     *   <li>6: List student enrollments</li>
     *   <li>7: Assign course grade</li>
     *   <li>8: Update student name</li>
     *   <li>9: Exit to previous menu</li>
     * </ol>
     *
     * @param scanner           Input scanner for user interactions
     * @param courseManagement  Course system instance (used for enrollment operations)
     * @param studentManagement Student system instance to execute operations
     * @param choice            Numeric menu selection (1-9)
     * @return {@code true} to continue student management session, {@code false} to exit
     *
     * @implNote
     * <ul>
     *   <li>Case 5 requires a valid {@link CourseManagement} instance for enrollment</li>
     *   <li>Input validation occurs before invocation (via {@link IOHelper#getIntInput})</li>
     *   <li>Case numbers tightly coupled with {@link #manageStudents} menu order</li>
     *   <li>Menu displays 8 options but accepts 1-9 (9 = exit)</li>
     *   <li>Uses {@link IOHelper} for input prompts and screen pauses</li>
     * </ul>
     */
    private static boolean handleStudentChoice(Scanner scanner, CourseManagement courseManagement, StudentManagement studentManagement, int choice) {
        switch (choice) {
            case 1 -> {
                studentManagement.listStudents();
                IOHelper.getStringInput(scanner, "\nPress ENTER to continue", true);
            }
            case 2 -> studentManagement.addStudent();
            case 3 -> studentManagement.removeStudent();
            case 4 -> studentManagement.restoreStudent();
            case 5 -> studentManagement.enrollStudentInCourse(courseManagement);
            case 6 -> studentManagement.listStudentEnrollments();
            case 7 -> studentManagement.assignGradeToStudent();
            case 8 -> studentManagement.updateStudentName();
            case 9 -> {
                return false;
            }
            default -> {
                System.out.println("Invalid choice. Try again.");
                IOHelper.wait(1);
            }
        }
        return true;
    }

    /**
     * Routes course management menu selections to appropriate operations.
     * <p>
     * This method acts as a dispatcher for course-related actions based on user input:
     * <ol>
     *   <li>1: List courses with pause</li>
     *   <li>2: Add new course</li>
     *   <li>3: Remove course</li>
     *   <li>4: Restore deleted course</li>
     *   <li>5: Calculate overall grade (potential menu misplacement)</li>
     *   <li>6: Update course name</li>
     *   <li>7: Update course capacity</li>
     *   <li>8: Exit to previous menu</li>
     * </ol>
     *
     * @param scanner          Input scanner for user interactions
     * @param courseManagement Course system instance to execute operations
     * @param choice           Numeric menu selection (1-8)
     * @return {@code true} to continue course management session, {@code false} to exit
     *
     * @implNote
     * <ul>
     *   <li>Case 5 ("Calculate Overall Grade") may belong in student management</li>
     *   <li>Input validation occurs before invocation (via {@link IOHelper#getIntInput})</li>
     *   <li>Case numbers tightly coupled with {@link #manageCourses} menu order</li>
     *   <li>Does not handle operation errors - delegates to CourseManagement methods</li>
     * </ul>
     */
    private static boolean handleCourseChoice(Scanner scanner, CourseManagement courseManagement, int choice) {
        switch (choice) {
            case 1 -> {
                courseManagement.listCourses();
                IOHelper.getStringInput(scanner, "\nPress ENTER to continue", true);
            }
            case 2 -> courseManagement.addCourse();
            case 3 -> courseManagement.removeCourse();
            case 4 -> courseManagement.restoreCourse();
            case 5 -> courseManagement.getOverallGrade();
            case 6 -> courseManagement.updateCourseName();
            case 7 -> courseManagement.updateCourseMaxCapacity();
            case 8 -> {
                return false;
            }
            default -> {
                System.out.println("Invalid choice. Try again.");
                IOHelper.wait(1);
            }
        }
        return true;
    }
}