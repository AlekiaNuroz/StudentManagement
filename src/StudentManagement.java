import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class StudentManagement {
    private final List<Student> students;
    private final Scanner scanner;
    private final DatabaseManager db;

    public StudentManagement(DatabaseManager db, Scanner scanner) {
        this.students = db.getStudents(false);
        this.scanner = scanner;
        this.db = db;
    }

    public void addStudent() {
        IOHelper.clearScreen();

        String StudentId = IOHelper.getStringInput(scanner, "Enter a student ID: ", false);

        if (db.getStudent(StudentId) != null) {
            System.out.println("Student already exists in the database.");
            return;
        }

        Student newStudent = createStudent(StudentId);

        if (db.insertStudent(newStudent)) {
            students.add(newStudent);
            System.out.println("Student added successfully.");
        } else {
            System.out.println("Failed to add student.");
        }
    }

    private Student createStudent(String studentId) {
        String name = IOHelper.getStringInput(scanner, "Enter the student's name: ", false);
        return new Student(studentId, name);
    }

    public void removeStudent() {
        IOHelper.clearScreen();
        listStudents();

        String id = IOHelper.getStringInput(scanner, "\nEnter a student id you wish to delete: ", false);

        students.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .ifPresentOrElse(student -> {
                    students.remove(student);
                    db.deleteRestoreStudent(student, true);
                    System.out.println("Student removed successfully.");
                }, () -> {
                    System.out.println("Student does not exist.");
                    IOHelper.wait(1);
                });
    }

    public void restoreStudent() {
        IOHelper.clearScreen();
        List<Student> deletedStudents = db.getStudents(true);

        deletedStudents.forEach(System.out::println);

        String code = IOHelper.getStringInput(scanner, "Enter a course code to restore: ", false);

        deletedStudents.stream()
                .filter(c -> c.getId().equals(code))
                .findFirst()
                .ifPresentOrElse(student -> {
                    students.add(student);
                    db.deleteRestoreStudent(student, false);
                }, () -> System.out.println("Student not found."));
    }

    public void listStudents() {
        IOHelper.clearScreen();

        int maxIdWidth = 0;
        int maxNameWidth = 0;

        if (!students.isEmpty()) {
            for (Student student : students) {
                maxIdWidth = Math.max(maxIdWidth, student.getId().length());
                maxNameWidth = Math.max(maxNameWidth, student.getName().length());
            }

            maxIdWidth = Math.max("Student ID".length(), maxIdWidth);

            // Format String
            String format = "| %-" + maxIdWidth + "s | %-" + maxNameWidth + "s |\n";

            // Print Header
            System.out.printf(format, "Student ID", "Student Name");
            System.out.println("|" + "-".repeat(maxIdWidth + 2) + "|" + "-".repeat(maxNameWidth + 2) + "|");

            // Print Students
            for (Student student : students) {
                System.out.printf(format, student.getId(), student.getName());
            }
        } else {
            System.out.println("No students found");
        }
        IOHelper.getStringInput(scanner, "\nPress ENTER to continue", true);
    }

    public Optional<Student> findStudentById(String studentId) {
        return students.stream().filter(s -> s.getId().equals(studentId)).findFirst();
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
