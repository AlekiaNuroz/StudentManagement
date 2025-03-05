import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles course management operations including listing, adding,
 * removing, and restoring courses.
 */
public class CourseManagement {
    private final List<Course> courses;
    private final Scanner scanner;
    private final DatabaseManager db;

    /**
     * Initializes CourseManagement with a database manager and scanner.
     *
     * @param db      The database manager for accessing course data.
     * @param scanner The scanner for user input.
     */
    public CourseManagement(DatabaseManager db, Scanner scanner) {
        this.db = db;
        this.courses = db.getCourses(false);
        AtomicInteger enrolled = new AtomicInteger();
        courses.forEach(course -> enrolled.addAndGet(course.getCurrentEnrollment()));
        Course.setTotalEnrolledStudents(enrolled.get());
        this.scanner = scanner;
    }

    /**
     * Displays a formatted table of courses stored in the system.
     * <p>
     * The table dynamically adjusts column widths based on the longest
     * course code and course name to ensure proper alignment. If no courses
     * are available, a message is displayed instead.
     * </p>
     *
     * <p>
     * The method clears the screen before displaying the table and waits for
     * user input before returning to the previous menu.
     * </p>
     *
     * <p><b>Behavior:</b></p>
     * <ul>
     *   <li>Finds the longest course code and course name to determine column width.</li>
     *   <li>Prints a table with a header and dynamically formatted course details.</li>
     *   <li>Displays "No courses found" if the list is empty.</li>
     *   <li>Prompts the user to press ENTER before returning.</li>
     * </ul>
     *
     * <p><b>Dependencies:</b></p>
     * <ul>
     *   <li>{@code IOHelper.clearScreen()} - Clears the console screen.</li>
     *   <li>{@code IOHelper.getStringInput()} - Waits for user input.</li>
     *   <li>{@code Course} - Represents a course with an ID, name, and max capacity.</li>
     * </ul>
     */
    public void listCourses() {
        IOHelper.clearScreen();

        int maxCodeWidth = 0;
        int maxNameWidth = 0;

        if (!courses.isEmpty()) {
            for (Course course : courses) {
                maxCodeWidth = Math.max(maxCodeWidth, course.getId().length());
                maxNameWidth = Math.max(maxNameWidth, course.getName().length());
            }

            maxCodeWidth = Math.max("Course Code".length(), maxCodeWidth);

            // Format String
            String format = "| %-" + maxCodeWidth + "s | %-" + maxNameWidth + "s | %-16s | %-12s |\n";

            // Print Header
            System.out.printf(format, "Course Code", "Course Name", "Current Capacity", "Max Capacity");
            System.out.println("|" + "-".repeat(maxCodeWidth + 2) + "|" + "-".repeat(maxNameWidth + 2) + "|" + "-".repeat(18) + "|" + "-".repeat(14) + "|");

            // Print Courses
            for (Course course : courses) {
                System.out.printf(format, course.getId(), course.getName(), course.getCurrentEnrollment(), course.getMaxCapacity());
            }

            System.out.println("\nTotal enrolled students: " + Course.getTotalEnrolledStudents());
        } else {
            System.out.println("No courses found");
        }
    }

    /**
     * Adds a new course to the system.
     * <p>
     * This method prompts the user to enter a course ID and checks if the course
     * already exists in the database. If the course does not exist, it creates
     * a new course and attempts to insert it into the database. If successful,
     * the course is added to the local list of courses.
     * </p>
     *
     * <p><b>Behavior:</b></p>
     * <ul>
     *   <li>Clears the screen before prompting for input.</li>
     *   <li>Requests a course ID from the user.</li>
     *   <li>Checks if the course already exists in the database.</li>
     *   <li>If the course does not exist, creates a new course and inserts it.</li>
     *   <li>Displays a success or failure message based on the database operation.</li>
     * </ul>
     *
     * <p><b>Dependencies:</b></p>
     * <ul>
     *   <li>{@code IOHelper.clearScreen()} - Clears the console screen.</li>
     *   <li>{@code IOHelper.getStringInput()} - Gets user input from the console.</li>
     *   <li>{@code db.getCourse(String)} - Checks if the course already exists.</li>
     *   <li>{@code createCourse(String)} - Creates a new {@code Course} object.</li>
     *   <li>{@code db.insertCourse(Course)} - Inserts a course into the database.</li>
     * </ul>
     *
     * @throws RuntimeException if an unexpected error occurs during database operations.
     */
    public void addCourse() {
        IOHelper.clearScreen();

        String id = IOHelper.getStringInput(scanner, "Enter a course ID: ", false);

        if (db.getCourse(id) != null) {
            System.out.println("Course already exists in the database.");
            return;
        }

        Course newCourse = createCourse(id);

        if (db.insertCourse(newCourse)) {
            courses.add(newCourse);
            System.out.println("Course added successfully.");
        } else {
            System.out.println("Failed to add course.");
        }
    }

    /**
     * Creates a new {@code Course} object with the given course ID.
     * <p>
     * This method prompts the user to enter a course name and a maximum capacity.
     * It ensures the capacity is within the valid range (1 to 100) and then
     * constructs a new {@code Course} object using the provided details.
     * </p>
     *
     * <p><b>Behavior:</b></p>
     * <ul>
     *   <li>Requests a course name from the user.</li>
     *   <li>Requests a maximum capacity, ensuring it is between 1 and 100.</li>
     *   <li>Creates and returns a new {@code Course} object.</li>
     * </ul>
     *
     * <p><b>Dependencies:</b></p>
     * <ul>
     *   <li>{@code IOHelper.getStringInput()} - Gets user input for the course name.</li>
     *   <li>{@code IOHelper.getIntInput()} - Gets user input for the course capacity.</li>
     *   <li>{@code Course} - Represents the course entity with an ID, name, and capacity.</li>
     * </ul>
     *
     * @param id The unique identifier for the course.
     * @return A new {@code Course} object containing the provided details.
     * @throws IllegalArgumentException if the input validation fails.
     */
    private Course createCourse(String id) {
        String name = IOHelper.getStringInput(scanner, "Enter a course name: ", false);
        int capacity = IOHelper.getIntInput(scanner, "Enter the maximum capacity: ", 1, 100, false, 10);
        return new Course(id, name, capacity);
    }

    /**
     * Removes a course from the system.
     * <p>
     * This method displays the list of available courses and prompts the user to
     * enter the course code of the course they wish to remove. If the course exists,
     * it is removed from the local list and marked as deleted in the database.
     * Otherwise, a message is displayed indicating that the course was not found.
     * </p>
     *
     * <p><b>Behavior:</b></p>
     * <ul>
     *   <li>Clears the screen before displaying the course list.</li>
     *   <li>Prompts the user to enter the course code to delete.</li>
     *   <li>Searches for the course in the list.</li>
     *   <li>If found, removes the course from the list and marks it as deleted in the database.</li>
     *   <li>If not found, displays an appropriate message and waits briefly.</li>
     * </ul>
     *
     * <p><b>Dependencies:</b></p>
     * <ul>
     *   <li>{@code IOHelper.clearScreen()} - Clears the console screen.</li>
     *   <li>{@code listCourses()} - Displays the available courses.</li>
     *   <li>{@code IOHelper.getStringInput()} - Gets user input for the course code.</li>
     *   <li>{@code db.deleteRestoreCourse(Course, boolean)} - Marks the course as deleted in the database.</li>
     *   <li>{@code IOHelper.wait(int)} - Pauses execution for a specified duration.</li>
     * </ul>
     */
    public void removeCourse() {
        IOHelper.clearScreen();
        listCourses();

        String code = IOHelper.getStringInput(scanner, "\nEnter a course code you wish to delete: ", false);

        courses.stream()
                .filter(c -> c.getId().equals(code))
                .findFirst()
                .ifPresentOrElse(course -> {
                    courses.remove(course);
                    db.deleteRestoreCourse(course, true);
                    System.out.println("Course removed successfully.");
                }, () -> {
                    System.out.println("Course does not exist.");
                    IOHelper.wait(1);
                });
    }

    /**
     * Restores a previously deleted course.
     * <p>
     * This method displays a list of deleted courses and prompts the user to enter
     * the course code of the course they wish to restore. If the course exists,
     * it is restored in the database and added back to the local list of courses.
     * Otherwise, an error message is displayed.
     * </p>
     *
     * <p><b>Behavior:</b></p>
     * <ul>
     *   <li>Clears the screen before displaying deleted courses.</li>
     *   <li>Prompts the user to enter the course code to restore.</li>
     *   <li>Checks if the course exists in the list of deleted courses.</li>
     *   <li>If found, restores the course in the database and adds it back to the active courses list.</li>
     *   <li>If not found, displays an appropriate error message.</li>
     * </ul>
     *
     * <p><b>Dependencies:</b></p>
     * <ul>
     *   <li>{@code IOHelper.clearScreen()} - Clears the console screen.</li>
     *   <li>{@code db.getDeletedCourses()} - Retrieves the list of deleted courses.</li>
     *   <li>{@code IOHelper.getStringInput()} - Gets user input for the course code.</li>
     *   <li>{@code db.deleteRestoreCourse(Course, boolean)} - Restores the course in the database.</li>
     * </ul>
     */
    public void restoreCourse() {
        IOHelper.clearScreen();
        List<Course> deletedCourses = db.getCourses(true);

        deletedCourses.forEach(System.out::println);

        String code = IOHelper.getStringInput(scanner, "Enter a course code to restore: ", false);

        deletedCourses.stream()
                .filter(c -> c.getId().equals(code))
                .findFirst()
                .ifPresentOrElse(course -> {
                    courses.add(course);
                    db.deleteRestoreCourse(course, false);
                }, () -> System.out.println("Course not found."));
    }
}