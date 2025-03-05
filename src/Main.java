import java.util.Scanner;

/**
 * Main class for the Student Management System.
 * It provides a command-line interface to manage courses and students.
 */
public class Main {

    /**
     * The entry point of the application.
     *
     * @param args Command-line arguments (not used).
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
     * Displays the main menu and handles user input.
     *
     * @param databaseManager The database manager instance.
     * @param scanner         The scanner for user input.
     * @return {@code false} if the user chooses to exit, otherwise {@code true}.
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
     * Manages course-related operations, such as listing, adding, and deleting courses.
     *
     * @param db      The database manager instance.
     * @param scanner The scanner for user input.
     */
    private static void manageCourses(DatabaseManager db, Scanner scanner) {
        CourseManagement courseManagement = new CourseManagement(db, scanner);
        boolean running = true;

        while (running) {
            IOHelper.clearScreen();
            IOHelper.printMenu("Manage Courses",
                    new String[]{"List Courses", "Add Course", "Delete Course", "Restore Course", "Calculate Overall Grade"});

            int choice = IOHelper.getIntInput(scanner, "Enter choice: ", 1, 6);
            running = handleCourseChoice(scanner, courseManagement, choice);
        }
    }

    private static void manageStudents(DatabaseManager db, CourseManagement courseManagement, Scanner scanner) {
        StudentManagement studentManagement = new StudentManagement(db, scanner);
        boolean running = true;

        while (running) {
            IOHelper.clearScreen();
            IOHelper.printMenu("Manage Courses",
                    new String[]{"List Students", "Add Student", "Delete Student", "Restore Student", "Enroll Student", "List Enrollments", "Add Grade"});

            int choice = IOHelper.getIntInput(scanner, "Enter choice: ", 1, 8);
            running = handleStudentChoice(scanner, courseManagement, studentManagement, choice);
        }
    }

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

    /**
     * Handles user choices related to course management.
     *
     * @param courseManagement The course management instance.
     * @param choice           The user’s menu choice.
     * @return {@code false} if the user chooses to exit course management, otherwise {@code true}.
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
            case 5 -> {
                System.out.println("Choice 5");
                IOHelper.wait(1);
            } // Placeholder for grade assignment
            case 6 -> {
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