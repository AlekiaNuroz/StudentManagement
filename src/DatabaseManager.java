import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages database operations for the student management system,
 * ensuring tables exist and handling course-related transactions.
 */
public class DatabaseManager {
    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource dataSource;

    static {
        config.setJdbcUrl(System.getenv("PGSQL_DATABASE"));
        config.setUsername(System.getenv("PGSQL_USERNAME"));
        config.setPassword(System.getenv("PGSQL_PASSWORD"));
        dataSource = new HikariDataSource(config);
    }

    /**
     * Initializes the database manager and ensures required tables exist.
     */
    public DatabaseManager() {
        ensureTablesExist();
    }

    /**
     * Establishes a connection to the SQLite database.
     *
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Ensures that the required database tables exist.
     * <p>
     * This method executes SQL statements to create the necessary tables
     * (`students`, `courses`, and `enrollments`) if they do not already exist.
     * It establishes a database connection, creates the tables, and handles
     * any SQL exceptions that may occur.
     * </p>
     *
     * <p><b>Tables Created:</b></p>
     * <ul>
     *   <li><b>students</b> - Stores student records with an ID and name.</li>
     *   <li><b>courses</b> - Stores course details with a unique course code, name,
     *       maximum capacity, and a deletion flag.</li>
     *   <li><b>enrollments</b> - Manages student-course enrollments,
     *       linking students to courses with an optional grade.</li>
     * </ul>
     *
     * <p><b>Behavior:</b></p>
     * <ul>
     *   <li>Retrieves a database connection.</li>
     *   <li>Executes SQL statements to create tables if they do not exist.</li>
     *   <li>Uses a try-with-resources block to ensure resources are properly closed.</li>
     *   <li>Catches and logs any SQL exceptions encountered during execution.</li>
     * </ul>
     *
     * <p><b>Dependencies:</b></p>
     * <ul>
     *   <li>{@code getConnection()} - Retrieves a database connection.</li>
     *   <li>{@code java.sql.Connection} - Manages the database connection.</li>
     *   <li>{@code java.sql.Statement} - Executes SQL queries.</li>
     *   <li>{@code SQLException} - Handles database-related exceptions.</li>
     * </ul>
     */
    private void ensureTablesExist() {
        String[] createTableSQLs = {
            "CREATE TABLE IF NOT EXISTS students ("
                + "id TEXT PRIMARY KEY, "
                + "name TEXT NOT NULL)",
    
            "CREATE TABLE IF NOT EXISTS courses ("
                + "course_code TEXT PRIMARY KEY, "
                + "name TEXT NOT NULL, "
                + "max_capacity INTEGER NOT NULL, "
                + "isDeleted BOOLEAN DEFAULT FALSE, "
                + "enrolled INTEGER DEFAULT 0)",
    
            "CREATE TABLE IF NOT EXISTS enrollments ("
                + "student_id TEXT, "
                + "course_code TEXT, "
                + "grade REAL, "
                + "PRIMARY KEY (student_id, course_code), "
                + "FOREIGN KEY(student_id) REFERENCES students(id), "
                + "FOREIGN KEY(course_code) REFERENCES courses(course_code))"
        };
    
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
    
            for (String sql : createTableSQLs) {
                statement.execute(sql);
            }
        } catch (SQLException e) {
            System.err.println("Error ensuring tables: " + e.getMessage());
        }
    }

    /**
     * Inserts a new course into the database.
     * <p>
     * This method adds a course record to the `courses` table with the given
     * course code, name, and maximum capacity. If the course already exists,
     * a message is displayed, and insertion is skipped.
     * </p>
     *
     * <p><b>Behavior:</b></p>
     * <ul>
     *   <li>Establishes a database connection.</li>
     *   <li>Prepares an SQL `INSERT` statement with placeholders for course details.</li>
     *   <li>Sets parameter values based on the provided {@code Course} object.</li>
     *   <li>Executes the statement and checks if any rows were affected.</li>
     *   <li>Handles integrity constraint violations if the course already exists.</li>
     *   <li>Catches and logs any SQL exceptions that may occur.</li>
     * </ul>
     *
     * <p><b>Dependencies:</b></p>
     * <ul>
     *   <li>{@code getConnection()} - Retrieves a database connection.</li>
     *   <li>{@code java.sql.Connection} - Manages the database connection.</li>
     *   <li>{@code java.sql.PreparedStatement} - Executes the parameterized SQL statement.</li>
     *   <li>{@code Course} - Represents the course object being inserted.</li>
     * </ul>
     *
     * @param course The {@code Course} object containing course details.
     * @return {@code true} if the course was successfully inserted, {@code false} otherwise.
     */
    public boolean insertCourse(Course course) {
        String insertSQL = "INSERT INTO courses (course_code, name, max_capacity) VALUES (?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(insertSQL)) {

            statement.setString(1, course.getId());
            statement.setString(2, course.getName());
            statement.setInt(3, course.getMaxCapacity());

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Course " + course.getId() + " already exists in the database.");
        } catch (SQLException e) {
            System.out.println("Error inserting course: " + e.getMessage());
        }
        return false;
    }

    /**
     * Marks a course as deleted or restores a previously deleted course in the database.
     * <p>
     * This method updates the `isDeleted` flag in the `courses` table to either
     * delete ({@code true}) or restore ({@code false}) a course based on the given parameter.
     * If the specified course does not exist, a message is displayed.
     * </p>
     *
     * <p><b>Behavior:</b></p>
     * <ul>
     *   <li>Establishes a database connection.</li>
     *   <li>Prepares an SQL `UPDATE` statement to modify the `isDeleted` field.</li>
     *   <li>Sets the deletion status based on the provided boolean value.</li>
     *   <li>Executes the update and checks if any rows were affected.</li>
     *   <li>Displays a message if the course does not exist.</li>
     *   <li>Catches and logs any SQL exceptions that may occur.</li>
     * </ul>
     *
     * <p><b>Dependencies:</b></p>
     * <ul>
     *   <li>{@code getConnection()} - Retrieves a database connection.</li>
     *   <li>{@code java.sql.Connection} - Manages the database connection.</li>
     *   <li>{@code java.sql.PreparedStatement} - Executes the parameterized SQL statement.</li>
     *   <li>{@code Course} - Represents the course object being updated.</li>
     * </ul>
     *
     * @param course The {@code Course} object representing the course to be deleted or restored.
     * @param delete {@code true} to mark the course as deleted, {@code false} to restore it.
     */
    public void deleteRestoreCourse(Course course, boolean delete) {
        String updateSQL = "UPDATE courses SET isDeleted = ? WHERE course_code = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(updateSQL)) {

            statement.setBoolean(1, delete);
            statement.setString(2, course.getId());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("Course " + course.getId() + " does not exist in the database.");
            }

        } catch (SQLException e) {
            System.out.println("Error updating course status: " + e.getMessage());
        }
    }

    public void deleteRestoreStudent(Student student, boolean delete) {
        String updateSQL = "UPDATE students SET isDeleted = ? WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(updateSQL)) {

            statement.setBoolean(1, delete);
            statement.setString(2, student.getId());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("Student " + student.getId() + " does not exist in the database.");
            }

        } catch (SQLException e) {
            System.out.println("Error updating student status: " + e.getMessage());
        }
    }

    /**
     * Retrieves a list of courses from the database based on their deletion status.
     * <p>
     * This method queries the `courses` table and returns a list of courses
     * that are either active ({@code isDeleted = false}) or deleted ({@code isDeleted = true}).
     * The results are ordered by course code.
     * </p>
     *
     * <p><b>Behavior:</b></p>
     * <ul>
     *   <li>Establishes a database connection.</li>
     *   <li>Prepares an SQL `SELECT` query to retrieve courses based on deletion status.</li>
     *   <li>Executes the query and iterates through the result set.</li>
     *   <li>Converts each row into a {@code Course} object using {@code getCourseFromResultSet()}.</li>
     *   <li>Handles SQL exceptions and returns an empty list in case of errors.</li>
     * </ul>
     *
     * <p><b>Dependencies:</b></p>
     * <ul>
     *   <li>{@code getConnection()} - Retrieves a database connection.</li>
     *   <li>{@code java.sql.Connection} - Manages the database connection.</li>
     *   <li>{@code java.sql.PreparedStatement} - Executes the parameterized SQL query.</li>
     *   <li>{@code java.sql.ResultSet} - Processes the query results.</li>
     *   <li>{@code getCourseFromResultSet(ResultSet)} - Converts a database row into a {@code Course} object.</li>
     * </ul>
     *
     * @param deleted {@code true} to retrieve deleted courses, {@code false} to retrieve active courses.
     * @return An {@code ArrayList<Course>} containing the retrieved courses.
     *         Returns an empty list if an error occurs.
     */
    public ArrayList<Course> getCourses(boolean deleted) {
        String selectSQL = "SELECT * FROM courses WHERE isDeleted=? ORDER BY course_code";
        ArrayList<Course> courses = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(selectSQL)) {

            statement.setBoolean(1, deleted);
             try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    courses.add(getCourseFromResultSet(resultSet));
                }
            }
            return courses;
        } catch (SQLException e) {
            System.out.println("Error getting courses: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves a course from the database by its course code.
     * <p>
     * This method queries the `courses` table for a course with the specified course code.
     * The lookup is case-insensitive, as the course code is converted to uppercase.
     * If the course exists, it is returned as a {@code Course} object;
     * otherwise, {@code null} is returned.
     * </p>
     *
     * <p><b>Behavior:</b></p>
     * <ul>
     *   <li>Establishes a database connection.</li>
     *   <li>Prepares an SQL `SELECT` query with a parameterized course code.</li>
     *   <li>Executes the query and checks if a result is found.</li>
     *   <li>Uses {@code getCourseFromResultSet(ResultSet)} to convert the row into a {@code Course} object.</li>
     *   <li>Handles SQL exceptions and prints an error message if an issue occurs.</li>
     * </ul>
     *
     * <p><b>Dependencies:</b></p>
     * <ul>
     *   <li>{@code getConnection()} - Retrieves a database connection.</li>
     *   <li>{@code java.sql.Connection} - Manages the database connection.</li>
     *   <li>{@code java.sql.PreparedStatement} - Executes the parameterized SQL query.</li>
     *   <li>{@code java.sql.ResultSet} - Processes the query results.</li>
     *   <li>{@code getCourseFromResultSet(ResultSet)} - Converts a database row into a {@code Course} object.</li>
     * </ul>
     *
     * @param id The course code to search for (case-insensitive).
     * @return The {@code Course} object if found; otherwise, {@code null}.
     */
    public Course getCourse(String id) {
        String sql = "SELECT * FROM courses WHERE UPPER(course_code) = ?";

        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, id.toUpperCase());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return getCourseFromResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching course: " + e.getMessage());
        }
        return null;
    }

    public List<Student> getStudents(boolean deleted) {
        String selectSQL = "SELECT * FROM students WHERE isDeleted=? ORDER BY id";
        ArrayList<Student> students = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(selectSQL)) {

            statement.setBoolean(1, deleted);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    students.add(getStudentFromResultSet(resultSet));
                }
            }
            return students;
        } catch (SQLException e) {
            System.out.println("Error getting students: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Student getStudent(String id) {
        String sql = "SELECT * FROM students WHERE UPPER(id) = ?";

        try (Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, id.toUpperCase());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return getStudentFromResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student: " + e.getMessage());
        }
        return null;
    }

    /**
     * Extracts a Course object from a ResultSet.
     *
     * @param resultSet ResultSet containing course data
     * @return Course object or null if an error occurs
     */
    private Course getCourseFromResultSet(ResultSet resultSet) {
        try {
            String id = resultSet.getString("course_code");
            String name = resultSet.getString("name");
            int capacity = resultSet.getInt("max_capacity");
            int enrolled = resultSet.getInt("enrolled");
            return new Course(id, name, capacity, enrolled);
        } catch (SQLException e) {
            System.err.println("Error retrieving course data from ResultSet: " + e.getMessage());
            return null; // Return null if there's an issue retrieving data
        }
    }

    private Student getStudentFromResultSet (ResultSet resultSet) {
        try {
            String studentId = resultSet.getString("id");
            String name = resultSet.getString("name");
            Student student = new Student(studentId, name);
            populateEnrollments(student);
            return student;
        } catch (SQLException e) {
            System.err.println("Error retrieving student data from ResultSet: " + e.getMessage());
            return null;
        }
    }

    public boolean insertStudent(Student student) {
        String insertSQL = "INSERT INTO students (id, name) VALUES (?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(insertSQL)) {

            statement.setString(1, student.getId());
            statement.setString(2, student.getName());

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Student " + student.getId() + " already exists in the database.");
        } catch (SQLException e) {
            System.out.println("Error inserting student: " + e.getMessage());
        }
        return false;
    }

    public boolean enrollStudentInCourse(String studentId, String courseCode) {
        String courseUpdateSQL = "UPDATE courses SET enrolled = enrolled + 1 "
                               + "WHERE course_code = ? AND enrolled < max_capacity";

        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(courseUpdateSQL)) {

            stmt.setString(1, courseCode);

            int rowsAffected = stmt.executeUpdate();
            if(rowsAffected == 0) {
                System.out.println("Student " + studentId + " could not be enrolled");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error enrolling student: " + e.getMessage());
            return false;
        }

        String insertSQL = "INSERT INTO enrollments (student_id, course_code, grade) VALUES (?, ?, NULL)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {

            stmt.setString(1, studentId);
            stmt.setString(2, courseCode);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Student " + studentId + " is already enrolled in course " + courseCode + ".");
            return false;
        } catch (SQLException e) {
            System.out.println("Error enrolling student in course: " + e.getMessage());
            return false;
        }
    }

    private void populateEnrollments(Student student) {
        String sql = "SELECT course_code, grade FROM enrollments WHERE student_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String courseCode = rs.getString("course_code");
                Double grade = rs.getDouble("grade");
                if (rs.wasNull()) {
                    grade = null;
                }
                Course course = getCourse(courseCode);
                if (course != null) {
                    student.getEnrolledCourses().put(course, grade);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error populating enrollments for student " + student.getId() + ": " + e.getMessage());
        }
    }

    public boolean assignGrade(String studentId, String courseCode, double grade) {
        String updateSql = "UPDATE enrollments SET grade = ? WHERE student_id = ? AND course_code = ?";

        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setDouble(1, grade);
            stmt.setString(2, studentId);
            stmt.setString(3, courseCode);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating enrollments for student " + studentId + ": " + e.getMessage());
            return false;
        }
    }
}