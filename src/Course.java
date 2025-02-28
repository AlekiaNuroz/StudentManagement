/**
 * Represents a course offered in the student management system.
 * A course has a unique course code, a name, a maximum capacity, and tracks current enrollment.
 */
public class Course {
    /** The unique identifier for the course. */
    private String courseCode;
    
    /** The name of the course. */
    private String name;
    
    /** The maximum number of students allowed in the course. */
    private int maxCapacity;
    
    /** The current number of enrolled students. */
    private int currentEnrollment;
    
    /** Tracks the total number of enrolled students across all courses. */
    private static int totalEnrolledStudents;

    /**
     * Constructs a new Course instance.
     * 
     * @param id The unique identifier for the course.
     * @param name The name of the course.
     * @param maxCapacity The maximum number of students allowed in the course.
     */
    public Course(String id, String name, int maxCapacity) {
        this.courseCode = id;
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.currentEnrollment = 0;
    }

    /**
     * Gets the course ID.
     * 
     * @return The unique course code.
     */
    public String getId() {
        return courseCode;
    }

    /**
     * Sets the course ID.
     * 
     * @param courseCode The new course code.
     */
    public void setId(String courseCode) {
        this.courseCode = courseCode;
    }

    /**
     * Gets the course name.
     * 
     * @return The name of the course.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the course name.
     * 
     * @param name The new course name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the maximum capacity of the course.
     * 
     * @return The maximum number of students allowed in the course.
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Sets the maximum capacity of the course.
     * 
     * @param maxCapacity The new maximum number of students.
     */
    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    /**
     * Gets the current number of enrolled students.
     * 
     * @return The number of students currently enrolled in the course.
     */
    public int getCurrentEnrollment() {
        return currentEnrollment;
    }

    /**
     * Gets the total number of enrolled students across all courses.
     * 
     * @return The total number of enrolled students.
     */
    public static int getTotalEnrolledStudents() {
        return totalEnrolledStudents;
    }

    /**
     * Checks if more students can be enrolled in the course.
     * 
     * @return true if enrollment is below max capacity, false otherwise.
     */
    public boolean canEnroll() {
        return currentEnrollment < maxCapacity;
    }

    /**
     * Increases the enrollment count if the course is not full.
     */
    public void increaseEnrollment() {
        if (currentEnrollment < maxCapacity) {
            currentEnrollment++;
            totalEnrolledStudents++;
        }
    }

    /**
     * Returns a string representation of the course.
     * 
     * @return A formatted string containing the course ID, name, and max capacity.
     */
    @Override
    public String toString() {
        return getId() + " - " + getName() + ": Max Capacity - " + getMaxCapacity();
    }
}
