import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        DatabaseManager databaseManager = new DatabaseManager();
        boolean running = true;

        while (running) {
            running = handleMainMenu(databaseManager, scanner);
        }

        scanner.close();
    }

    private static boolean handleMainMenu(DatabaseManager databaseManager, Scanner scanner) {
        IOHelper.clearScreen();
        IOHelper.printMenu("Welcome to the Student Management System",
                new String[]{"Manage Courses", "Manage Students"});

        int choice = IOHelper.getIntInput(scanner, "Enter choice: ", 1, 3);
        switch (choice) {
            case 1:
                manageCourses(databaseManager, scanner);
                break;
            case 2:
                System.out.println("Choice 2"); // Placeholder for student management logic
                break;
            case 3:
                return false; // Exit the program
            default:
                System.out.println("Invalid choice. Try again.");
        }
        IOHelper.wait(1);
        return true;
    }

    private static void manageCourses(DatabaseManager db, Scanner scanner) {
        CourseManagement courseManagement = new CourseManagement(db, scanner);
        boolean running = true;

        while (running) {
            IOHelper.clearScreen();
            IOHelper.printMenu("Manage Courses",
                    new String[]{"List Courses", "Add Course", "Delete Course", "Restore Course", "Assign Grade", "Calculate Overall Grade"});

            int choice = IOHelper.getIntInput(scanner, "Enter choice: ", 1, 7);
            running = handleCourseChoice(courseManagement, choice);
        }
    }

    private static boolean handleCourseChoice(CourseManagement courseManagement, int choice) {
        switch (choice) {
            case 1 -> courseManagement.listCourses();
            case 2 -> courseManagement.addCourse();
            case 3 -> courseManagement.removeCourse();
            case 4 -> courseManagement.restoreCourse();
            case 5 -> System.out.println("Choice 5"); // Placeholder for grade assignment
            case 6 -> System.out.println("Choice 6"); // Placeholder for grade calculation
            case 7 -> { return false; } // Exit course management
            default -> System.out.println("Invalid choice. Try again.");
        }
        IOHelper.wait(1);
        return true;
    }
}