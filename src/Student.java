import java.util.HashMap;
import java.util.Map;

public class Student {
    private String name;
    private String studentId;
    private Map<Course, Double> enrolledCourses; // Course -> Grade

    public Student(String name, String studentId) {
        this.name = name;
        this.studentId = studentId;
        this.enrolledCourses = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public String getStudentId() {
        return studentId;
    }

    public Map<Course, Double> getEnrolledCourses() {
        return enrolledCourses;
    }

    public void enrollInCourse(Course course) {
        if (!enrolledCourses.containsKey(course)) {
            enrolledCourses.put(course, null); // Grade is initially null
            System.out.println(name + " enrolled in " + course.getName());
        } else {
            System.out.println(name + " is already enrolled in " + course.getName());
        }
    }

    public void assignGrade(Course course, double grade) {
        if (enrolledCourses.containsKey(course)) {
            enrolledCourses.put(course, grade);
            System.out.println("Assigned grade " + grade + " to " + name + " for " + course.getName());
        } else {
            System.out.println(name + " is not enrolled in " + course.getName());
        }
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", studentId='" + studentId + '\'' +
                ", enrolledCourses=" + enrolledCourses +
                '}';
    }
}
