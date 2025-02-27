import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentManagement {
    private List<Student> students;

    public StudentManagement() {
        this.students = new ArrayList<>();
    }

    public void addStudent(String name, String studentId) {
        students.add(new Student(name, studentId));
        System.out.println("Student " + name + " added successfully.");
    }

    public void listStudents() {
        if (students.isEmpty()) {
            System.out.println("No students available.");
        } else {
            for (Student student : students) {
                System.out.println(student.getStudentId() + " - " + student.getName());
            }
        }
    }

    public Optional<Student> findStudentById(String studentId) {
        return students.stream().filter(s -> s.getStudentId().equals(studentId)).findFirst();
    }

    public void enrollStudentInCourse(String studentId, Course course) {
        Optional<Student> studentOpt = findStudentById(studentId);
        if (studentOpt.isPresent()) {
            studentOpt.get().enrollInCourse(course);
        } else {
            System.out.println("Student not found.");
        }
    }

    public void assignGradeToStudent(String studentId, Course course, double grade) {
        Optional<Student> studentOpt = findStudentById(studentId);
        if (studentOpt.isPresent()) {
            studentOpt.get().assignGrade(course, grade);
        } else {
            System.out.println("Student not found.");
        }
    }
}
