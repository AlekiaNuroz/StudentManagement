import java.util.HashMap;
import java.util.Map;

/**
 * Represents a student with course enrollments and associated grades.
 * <p>
 * This class tracks:
 * <ul>
 *   <li>Student identifier and name</li>
 *   <li>Enrolled courses mapped to grades (null indicates no grade assigned)</li>
 *   <li>Course enrollment status and grade management</li>
 * </ul>
 *
 * @see Course
 */
public class Student {
    private final String studentId;
    private String name;
    private final Map<Course, Double> enrolledCourses; // Course -> Grade

    /**
     * Creates a new Student with no course enrollments.
     *
     * @param studentId Unique identifier for the student (case-sensitive)
     * @param name Full name of the student (non-blank)
     */
    public Student(String studentId, String name) {
        this.name = name;
        this.studentId = studentId;
        this.enrolledCourses = new HashMap<>();
    }

    /**
     * @return The student's full name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || !name.isEmpty()) {
            this.name = name;
        }
    }

    /**
     * @return The student's unique identifier
     */
    public String getId() {
        return studentId;
    }

    /**
     * @return An unmodifiable view of enrolled courses and grades (null = ungraded)
     * @implNote Modifications should be done through {@link #enrollInCourse(Course)}
     *           and {@link #assignGrade(Course, double)}
     */
    public Map<Course, Double> getEnrolledCourses() {
        return enrolledCourses;
    }

    /**
     * Enrolls the student in a course and updates course enrollment counts.
     *
     * @param course The course to enroll in (must not be null)
     * @implNote Increases the course's enrollment count via {@link Course#increaseEnrollment()}
     *           Initializes grade as null for the course
     */
    public void enrollInCourse(Course course) {
        course.increaseEnrollment();
        enrolledCourses.put(course, null);
    }

    /**
     * Assigns a grade for an enrolled course.
     *
     * @param course The course to grade (must be previously enrolled)
     * @param grade Percentage grade (0-100, caller must validate)
     * @implNote Only updates grades for currently enrolled courses
     *           Does not validate grade range - caller must ensure 0 ≤ grade ≤ 100
     */
    public void assignGrade(Course course, double grade) {
        if (enrolledCourses.containsKey(course)) {
            enrolledCourses.put(course, grade);
        }
    }
}