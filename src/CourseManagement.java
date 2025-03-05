import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages course-related operations including creation, deletion, listing, and enrollment tracking.
 * <p>
 * This class serves as the primary interface between course data, user input, and database persistence.
 * Key features include:
 * <ul>
 *   <li>Maintaining an in-memory list of active courses synchronized with the database</li>
 *   <li>Tracking global enrollment statistics via {@link Course} static properties</li>
 *   <li>Coordinating database operations through {@link DatabaseManager}</li>
 * </ul>
 *
 * @see DatabaseManager
 * @see Course
 */
public class CourseManagement {
    private final List<Course> courses;
    private final Scanner scanner;
    private final DatabaseManager db;

    /**
     * Initializes course management with database-backed data and enrollment statistics.
     * <p>
     * During construction:
     * <ol>
     *   <li>Loads active (non-deleted) courses via {@link DatabaseManager#getCourses(boolean)} with {@code false}</li>
     *   <li>Calculates total enrolled students across all courses</li>
     *   <li>Initializes static enrollment counter via {@link Course#setTotalEnrolledStudents(int)}</li>
     *   <li>Stores references to database manager and input scanner</li>
     * </ol>
     *
     * @param db The database manager for persistence operations
     * @param scanner The input scanner for user interactions
     *
     * @implNote The enrollment total is calculated by summing {@link Course#getCurrentEnrollment()} from all courses.
     *           Ensures static enrollment counter reflects persisted data at initialization. Not thread-safe -
     *           concurrent access to static enrollment counter requires external synchronization.
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
     * Displays all active courses in a formatted table with dynamic column widths and enrollment statistics.
     * <p>
     * This method:
     * <ol>
     *   <li>Clears the screen via {@link IOHelper#clearScreen()}</li>
     *   <li>Calculates column widths based on:
     *     <ul>
     *       <li>Longest course ID and name in the list</li>
     *       <li>Header text ("Course Code" and "Course Name")</li>
     *     </ul>
     *   </li>
     *   <li>Prints a table with:
     *     <ul>
     *       <li>Header row with four columns: Code, Name, Current Capacity, Max Capacity</li>
     *       <li>Separator line using hyphens</li>
     *       <li>Rows showing course details with aligned columns</li>
     *     </ul>
     *   </li>
     *   <li>Displays total enrolled students across all courses using {@link Course#getTotalEnrolledStudents()}</li>
     *   <li>Shows "No courses found" if the list is empty</li>
     * </ol>
     *
     * @implNote The table formatting uses printf with dynamic width specifiers.
     *           Column widths are determined via linear scan of all courses (O(n) time complexity).
     *           Console output assumes monospaced font for proper alignment. Total enrollment
     *           is retrieved from a static counter in {@link Course} which may need synchronization
     *           in concurrent environments.
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
     * Interactively adds a new course to both the in-memory collection and database after validation checks.
     * <p>
     * This method executes the following workflow:
     * <ol>
     *   <li>Clears the console using {@link IOHelper#clearScreen()}</li>
     *   <li>Prompts for a course ID and verifies uniqueness via {@link DatabaseManager#getCourse(String)}</li>
     *   <li>If the course exists:
     *     <ul>
     *       <li>Displays "Course already exists" error and aborts operation</li>
     *     </ul>
     *   </li>
     *   <li>If new:
     *     <ul>
     *       <li>Collects course details (name, capacity) via {@link #createCourse(String)}</li>
     *       <li>Persists to database using {@link DatabaseManager#insertCourse(Course)}</li>
     *       <li>On success: adds to in-memory {@code courses} list and displays confirmation</li>
     *       <li>On failure: displays "Failed to add course" error</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * @implNote Maintains synchronization between database and in-memory data.
     *           Course ID validation is case-sensitive. Requires the database to enforce
     *           unique constraint on course IDs for complete data integrity.
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
     * Interactively constructs a {@link Course} by collecting required fields through user input.
     * <p>
     * This method:
     * <ul>
     *   <li>Collects course name via {@link IOHelper#getStringInput} with non-blank enforcement</li>
     *   <li>Gets capacity with validation via {@link IOHelper#getIntInput}:
     *     <ul>
     *       <li>Accepts values between 1-100 (inclusive)</li>
     *       <li>Uses default value 10 if input is invalid/empty (when allowed)</li>
     *     </ul>
     *   </li>
     *   <li>Constructs a {@link Course} with the provided ID, collected name, and validated capacity</li>
     * </ul>
     *
     * @param id The pre-validated course ID to assign (typically generated externally)
     * @return A fully initialized {@link Course} object with guaranteed valid fields
     *
     * @implNote This is a helper method for centralized course creation during add/restore workflows.
     *           Input validation ensures minimum capacity (1) and prevents unreasonably large classes (100).
     *           Does NOT persist to database - caller must handle storage operations.
     */
    private Course createCourse(String id) {
        String name = IOHelper.getStringInput(scanner, "Enter a course name: ", false);
        int capacity = IOHelper.getIntInput(scanner, "Enter the maximum capacity: ", 1, 100, false, 10);
        return new Course(id, name, capacity);
    }

    /**
     * Soft-deletes a course by removing it from the active list and updating its database status.
     * <p>
     * This method:
     * <ol>
     *   <li>Clears the screen via {@link IOHelper#clearScreen()}</li>
     *   <li>Displays active courses using {@link #listCourses()}</li>
     *   <li>Prompts for a course code to delete</li>
     *   <li>Searches for the course in the active list:
     *     <ul>
     *       <li>If found:
     *         <ul>
     *           <li>Removes from the in-memory {@code courses} collection</li>
     *           <li>Persists deletion via {@link DatabaseManager#deleteRestoreCourse(Course, boolean)} with {@code true}</li>
     *           <li>Displays success message</li>
     *         </ul>
     *       </li>
     *       <li>If not found, displays error message and pauses via {@link IOHelper#wait(int)}</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * @implNote Implements a soft-delete pattern - the course is marked as deleted in persistence but retained in the database.
     *           Uses a linear search (O(n) time) through the courses list. Maintains consistency between in-memory state
     *           and database records.
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
     * Restores a previously soft-deleted course from the database and adds it back to the active course list.
     * <p>
     * This method performs the following operations:
     * <ol>
     *   <li>Clears the screen using {@link IOHelper#clearScreen()}</li>
     *   <li>Retrieves all soft-deleted courses via {@link DatabaseManager#getCourses(boolean)} with {@code true}</li>
     *   <li>Displays the list of deleted courses</li>
     *   <li>Prompts for a course code to restore</li>
     *   <li>Searches deleted courses for a matching ID:
     *     <ul>
     *       <li>If found:
     *         <ul>
     *           <li>Adds course back to active {@code courses} collection</li>
     *           <li>Updates database status via {@link DatabaseManager#deleteRestoreCourse(Course, boolean)} with {@code false}</li>
     *         </ul>
     *       </li>
     *       <li>If not found, displays "Course not found" message</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * @implNote The {@code true} parameter in {@code getCourses(true)} indicates retrieval of deleted records.
     *           Maintains synchronization between in-memory state and database persistence.
     *           Uses case-sensitive course ID matching for restoration.
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