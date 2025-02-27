import java.util.List;
import java.util.Scanner;

public class CourseManagement {
    private List<Course> courses;
    private final Scanner scanner;
    private final DatabaseManager db;

    public CourseManagement(DatabaseManager db, Scanner scanner) {
        this.db = db;
        this.courses = db.getCourses(false);
        this.scanner = scanner;
    }

    public void listCourses() {
        if (!courses.isEmpty()) {
            for (Course course : courses) {
                System.out.println(course.toString());
            }
        } else {
            System.out.println("No courses found");
        }
    }

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

    private Course createCourse(String id) {
        String name = IOHelper.getStringInput(scanner, "Enter a course name: ", false);
        int capacity = IOHelper.getIntInput(scanner, "Enter the maximum capacity: ", 1, 100, false, 10);
        return new Course(id, name, capacity);
    }

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
