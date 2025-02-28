import java.util.List;
import java.util.Scanner;

/**
 * Handles course management operations including listing, adding,
 * removing, and restoring courses.
 */
public class CourseManagement {
    private List<Course> courses;
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
        this.scanner = scanner;
    }

    /**
     * Lists all available courses.
     * If no courses are found, a message is displayed.
     */
    public void listCourses() {
        if (!courses.isEmpty()) {
            for (Course course : courses) {
                System.out.println(course.toString());
            }
        } else {
            System.out.println("No courses found");
        }
    }

    /**
     * Adds a new course to the database and updates the local list.
     * If the course already exists, an error message is displayed.
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
     * Creates a new course based on user input.
     *
     * @param id The course ID.
     * @return The newly created Course object.
     */
    private Course createCourse(String id) {
        String name = IOHelper.getStringInput(scanner, "Enter a course name: ", false);
        int capacity = IOHelper.getIntInput(scanner, "Enter the maximum capacity: ", 1, 100, false, 10);
        return new Course(id, name, capacity);
    }

    /**
     * Removes a course from the database and updates the local list.
     * If the course does not exist, an error message is displayed.
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
     * Displays a list of deleted courses and allows the user to restore one.
     * If the course is not found, an error message is displayed.
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
