import java.util.Objects;

/**
 * Represents an academic course with enrollment management capabilities.
 * <p>
 * Tracks both per-course enrollment limits and global enrollment statistics across all courses.
 * Key features include:
 * <ul>
 *   <li>Course identification code and name</li>
 *   <li>Maximum capacity enforcement</li>
 *   <li>Current enrollment tracking</li>
 *   <li>Global enrollment statistics via static counter</li>
 * </ul>
 */
public class Course {
    private final String courseCode;
    private String name;
    private int maxCapacity;
    private int currentEnrollment;
    private static int totalEnrolledStudents;

    /**
     * Creates a course with specified code, name, and maximum capacity (initial enrollment set to 0).
     *
     * @param id Unique course identifier code
     * @param name Full course name
     * @param maxCapacity Maximum allowed students (must be ≥ 0)
     */
    public Course(String id, String name, int maxCapacity) {
        this(id, name, maxCapacity, 0);
    }

    /**
     * Creates a course with full initialization parameters.
     *
     * @param id Unique course identifier code
     * @param name Full course name
     * @param maxCapacity Maximum allowed students (must be ≥ currentEnrollment)
     * @param currentEnrollment Initial enrolled student count (must be ≥ 0)
     */
    public Course(String id, String name, int maxCapacity, int currentEnrollment) {
        this.courseCode = id;
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.currentEnrollment = currentEnrollment;

    }

    /**
     * @return The course's unique identifier code
     */
    public String getId() {
        return courseCode;
    }

    /**
     * @return The course's display name
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the course display name.
     *
     * @param name New course name (must not be null/empty)
     */
    public void setName(String name) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
        }
    }

    /**
     * @return Maximum allowed student capacity
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Updates course capacity.
     *
     * @param maxCapacity New maximum capacity (must be ≥ currentEnrollment)
     * @implNote Does not automatically adjust current enrollment if capacity decreases
     */
    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    /**
     * @return Current number of enrolled students
     */
    public int getCurrentEnrollment() {
        return currentEnrollment;
    }

    /**
     * Sets global enrollment counter across all courses.
     *
     * @param value New total enrollment count (should match sum of all course enrollments)
     * @implNote This static value requires manual synchronization with actual enrollments
     */
    public static void setTotalEnrolledStudents(int value) {
        totalEnrolledStudents = value;
    }

    /**
     * @return Total students enrolled across all courses
     */
    public static int getTotalEnrolledStudents() {
        return totalEnrolledStudents;
    }

    /**
     * Checks if enrollment is still available.
     *
     * @return true if current enrollment < max capacity, false otherwise
     */
    public boolean canEnroll() {
        return currentEnrollment < maxCapacity;
    }

    /**
     * Increments enrollment counters if capacity allows.
     * <p>
     * Affects both:
     * <ul>
     *   <li>This course's current enrollment</li>
     *   <li>Global total enrollment counter</li>
     * </ul>
     * @implNote Thread-safe increment not guaranteed for static total
     */
    public void increaseEnrollment() {
        if (canEnroll()) {
            currentEnrollment++;
            totalEnrolledStudents++;
        }
    }

    /**
     * Compares courses based on unique identifier code.
     *
     * @param obj Object to compare
     * @return true if same course code (case-sensitive), false otherwise
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Course course = (Course) obj;
        return courseCode.equals(course.courseCode); // Assuming courseCode uniquely identifies a course
    }

    /**
     * Generates hash code based on course code.
     *
     * @return Hash code of course identifier
     */
    @Override
    public int hashCode() {
        return Objects.hash(courseCode);
    }
}