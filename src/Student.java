import java.util.HashMap;
import java.util.Map;

/**
 * Represents a student with a name, student ID, and enrolled courses.
 */
public class Student {
    private String studentId;
    private String name;
    private Map<Course, Double> enrolledCourses; // Course -> Grade

    /**
     * Constructs a Student object with the given name and student ID.
     *
     * @param name      The name of the student.
     * @param studentId The unique identifier for the student.
     */
    public Student(String studentId, String name) {
        this.name = name;
        this.studentId = studentId;
        this.enrolledCourses = new HashMap<>();
    }

    /**
     * Gets the student's name.
     *
     * @return The name of the student.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the student's unique ID.
     *
     * @return The student ID.
     */
    public String getId() {
        return studentId;
    }

    /**
     * Gets the courses the student is enrolled in along with their grades.
     *
     * @return A map of courses to grades.
     */
    public Map<Course, Double> getEnrolledCourses() {
        return enrolledCourses;
    }

    /**
     * Enrolls the student in a course if not already enrolled.
     *
     * @param course The course to enroll in.
     */
    public void enrollInCourse(Course course) {
        if (!enrolledCourses.containsKey(course)) {
            enrolledCourses.put(course, null); // Grade is initially null
            System.out.println(name + " enrolled in " + course.getName());
        } else {
            System.out.println(name + " is already enrolled in " + course.getName());
        }
    }

    /**
     * Assigns a grade to the student for a specific course.
     *
     * @param course The course for which the grade is assigned.
     * @param grade  The grade to assign.
     */
    public void assignGrade(Course course, double grade) {
        if (enrolledCourses.containsKey(course)) {
            enrolledCourses.put(course, grade);
            System.out.println("Assigned grade " + grade + " to " + name + " for " + course.getName());
        } else {
            System.out.println(name + " is not enrolled in " + course.getName());
        }
    }

    /**
     * Returns a string representation of the Student object.
     *
     * @return A string containing the student's details and enrolled courses.
     */
    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", studentId='" + studentId + '\'' +
                ", enrolledCourses=" + enrolledCourses +
                '}';
    }
}
