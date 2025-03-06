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

            statement.setString(1, course.getId().toUpperCase());
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
        String updateSQL = "UPDATE courses SET isDeleted = ? WHERE UPPER(course_code) = ?";

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
        String updateSQL = "UPDATE students SET isDeleted = ? WHERE UPPER(id) = ?";

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

    /**
     * Retrieves students from the database filtered by deletion status.
     * <p>
     * This method:
     * <ul>
     *   <li>Executes a SQL query to fetch active (undeleted) or deleted students based on the parameter</li>
     *   <li>Orders results by student ID in ascending order</li>
     *   <li>Maps database rows to {@link Student} objects using {@link #getStudentFromResultSet(ResultSet)}</li>
     *   <li>Handles database resources automatically via try-with-resources</li>
     * </ul>
     *
     * @param deleted {@code true} to retrieve soft-deleted students, {@code false} for active students
     * @return A modifiable {@link List} of students (empty if no matches or errors occur)
     *
     * @implNote Uses prepared statements to prevent SQL injection. Returns an empty list (never null)
     *           on database errors while logging the exception message. Results are ordered by
     *           the database's natural ID ordering. Connection is acquired via {@link #getConnection()}.
     */
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

    /**
     * Retrieves a student from the database using a case-insensitive ID search.
     * <p>
     * This method:
     * <ul>
     *   <li>Performs a case-insensitive match on student ID using SQL UPPER() function</li>
     *   <li>Returns the first matching student record</li>
     *   <li>Returns null if no match found or on database errors</li>
     * </ul>
     *
     * @param id The student ID to search for (case-insensitive match)
     * @return The matching {@link Student} object, or null if not found/error occurs
     *
     * @implNote Uses prepared statements for SQL injection prevention. Converts input ID
     *           to uppercase for case-insensitive comparison. Database connections and
     *           statements are auto-closed via try-with-resources. Errors are logged to
     *           stderr but not propagated to caller.
     */
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
     * Creates a {@link Course} object from the current row of a {@link ResultSet}.
     * <p>
     * This method extracts column values for:
     * <ul>
     *   <li>Course code (alias "course_code")</li>
     *   <li>Course name (alias "name")</li>
     *   <li>Maximum capacity (alias "max_capacity")</li>
     *   <li>Current enrollment (alias "enrolled")</li>
     * </ul>
     * and constructs a {@link Course} with these values.
     *
     * @param resultSet The result set containing course data (cursor should be positioned on a valid row)
     * @return A fully initialized {@link Course} object, or {@code null} if data retrieval fails
     *
     * @implNote This is a helper method for database mapping operations.
     *           Errors are logged to stderr but not propagated. Callers must check for null returns.
     *           Column names must match database schema/query aliases exactly.
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

    /**
     * Constructs a {@link Student} with enrollments from the current row of a {@link ResultSet}.
     * <p>
     * This method:
     * <ul>
     *   <li>Extracts student ID and name from "id" and "name" columns</li>
     *   <li>Initializes a {@link Student} with these values</li>
     *   <li>Populates enrollments via {@link #populateEnrollments(Student)}</li>
     * </ul>
     *
     * @param resultSet The result set positioned at a valid student row
     * @return Fully initialized {@link Student} with enrollments, or {@code null} on errors
     *
     * @implNote Requires exact column name matching in the result set ("id", "name").
     *           Errors are logged to stderr but not propagated. The caller must:
     *           <ul>
     *             <li>Position the result set cursor before invocation</li>
     *             <li>Handle null returns indicating data retrieval failures</li>
     *           </ul>
     */
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

    /**
     * Inserts a new student record into the database.
     * <p>
     * This method:
     * <ul>
     *   <li>Attempts to insert the student's ID and name into the {@code students} table</li>
     *   <li>Handles duplicate IDs via {@link SQLIntegrityConstraintViolationException}</li>
     *   <li>Uses parameterized queries to prevent SQL injection</li>
     * </ul>
     *
     * @param student The {@link Student} object to persist (must have valid ID/name)
     * @return {@code true} if insertion succeeded (1+ rows affected), {@code false} on failure/duplicate
     *
     * @implNote Uses try-with-resources for automatic connection/statement management.
     *           Duplicate IDs trigger a specific error message. Does NOT update in-memory
     *           student lists - caller must manage this separately. Database connection
     *           is acquired via {@link #getConnection()}.
     */
    public boolean insertStudent(Student student) {
        String insertSQL = "INSERT INTO students (id, name) VALUES (?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(insertSQL)) {

            statement.setString(1, student.getId().toUpperCase());
            statement.setString(2, student.getName().toUpperCase());

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Student " + student.getId() + " already exists in the database.");
        } catch (SQLException e) {
            System.out.println("Error inserting student: " + e.getMessage());
        }
        return false;
    }

    /**
     * Enrolls a student in a course with capacity validation and duplicate enrollment checks.
     * <p>
     * This method performs a two-step operation:
     * <ol>
     *   <li>Attempts to increment the course's enrollment count if capacity allows</li>
     *   <li>If successful, creates an enrollment record with a null grade</li>
     * </ol>
     *
     * @param studentId The ID of the student to enroll (case-sensitive)
     * @param courseCode The course code to enroll in (case-sensitive)
     * @return {@code true} if enrollment succeeded in both steps, {@code false} if:
     * <ul>
     *   <li>Course is at maximum capacity</li>
     *   <li>Duplicate enrollment exists</li>
     *   <li>Database errors occur</li>
     * </ul>
     *
     * @implNote Uses separate database connections for each operation - not atomic.
     *           Potential inconsistency if course enrollment succeeds but enrollment record insertion fails.
     *           Checks course capacity via SQL condition {@code enrolled < max_capacity}.
     *           Duplicate enrollments are detected through primary key constraints.
     */
    public boolean enrollStudentInCourse(String studentId, String courseCode) {
        String courseUpdateSQL = "UPDATE courses SET enrolled = enrolled + 1 "
                               + "WHERE UPPER(course_code) = ? AND enrolled < max_capacity";

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

            stmt.setString(1, studentId.toUpperCase());
            stmt.setString(2, courseCode.toUpperCase());

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

    /**
     * Populates a student's enrolled courses and grades from database records.
     * <p>
     * This method:
     * <ul>
     *   <li>Queries the database for all enrollments associated with the student</li>
     *   <li>Maps course codes to {@link Course} objects via {@link #getCourse(String)}</li>
     *   <li>Handles NULL grade values from the database as null references</li>
     *   <li>Silently skips courses that no longer exist in the system</li>
     * </ul>
     *
     * @param student The student whose enrollments will be populated (must have valid ID)
     *
     * @implNote Uses separate database connection via {@link #getConnection()}.
     *           Grades are stored as Double (nullable) in the student's enrollment map.
     *           Errors are logged to stderr but not propagated to the caller.
     *           Requires "enrollments" table with student_id, course_code, and grade columns.
     */
    private void populateEnrollments(Student student) {
        String sql = "SELECT course_code, grade FROM enrollments WHERE UPPER(student_id) = ?";
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

    /**
     * Assigns/updates a grade for a student's course enrollment in the database.
     * <p>
     * This method directly updates the grade column in the enrollments table without
     * validating course enrollment status or grade range constraints.
     *
     * @param studentId The ID of the student to grade (case-insensitive match)
     * @param courseCode The course code to update (case-insensitive match)
     * @param grade Numeric grade value to assign (caller must validate range)
     * @return {@code true} if exactly one enrollment record was updated,
     *         {@code false} if no matching enrollment exists or errors occur
     *
     * @implNote Does NOT verify:
     * <ul>
     *   <li>Existence of student/course</li>
     *   <li>Grade validity (0-100 range)</li>
     *   <li>Current enrollment status</li>
     * </ul>
     * Uses exact case-sensitive matches for both student_id and course_code.
     * Errors are logged to stderr but not rethrown.
     */
    public boolean assignGrade(String studentId, String courseCode, double grade) {
        String updateSql = "UPDATE enrollments SET grade = ? WHERE UPPER(student_id) = ? AND UPPER(course_code) = ?";

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

    /**
     * Updates the name of a student in the database.
     * <p>
     * This method directly modifies the student's name using a case-sensitive match on their ID.
     *
     * @param studentId The ID of the student to update (case-insensitive)
     * @param newName The new full name to assign (non-blank, validation is caller's responsibility)
     * @return {@code true} if exactly 1 student record was updated,
     *         {@code false} if no matching student exists or errors occur
     *
     * @implNote
     * <ul>
     *   <li>Uses parameterized queries to prevent SQL injection</li>
     *   <li>Does not validate name format/length - caller must ensure validity</li>
     *   <li>Silently fails if student ID doesn't exist</li>
     *   <li>Errors are logged to stderr but not rethrown</li>
     * </ul>
     */
    public boolean updateStudentName(String studentId, String newName) {
        String updateSql = "UPDATE students SET name = ? WHERE UPPER(id) = ?";

        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(updateSql)) {

            stmt.setString(1, newName);
            stmt.setString(2, studentId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating student name: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates the name of a course in the database identified by its course code.
     * <p>
     * This method performs a direct update of the course's name using a case-sensitive match on the course code.
     *
     * @param courseCode The unique identifier of the course to update (case-insensitive)
     * @param newName The new name to assign to the course (validation is caller's responsibility)
     * @return {@code true} if 1 or more rows were updated (success), {@code false} if:
     * <ul>
     *   <li>No course matches the provided code</li>
     *   <li>A database error occurs</li>
     * </ul>
     *
     * @implNote
     * <ul>
     *   <li>Uses parameterized queries to prevent SQL injection</li>
     *   <li>Does not validate newName format/length - caller must ensure validity</li>
     *   <li>Silently fails if the course code does not exist</li>
     *   <li>Errors are logged to stderr but not propagated</li>
     * </ul>
     */
    public boolean updateCourseName(String courseCode, String newName) {
        String updateSql = "UPDATE courses SET name = ? WHERE UPPER(course_code) = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {

            stmt.setString(1, newName);
            stmt.setString(2, courseCode);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating course name: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates the maximum capacity of a specified course in the database.
     * <p>
     * This method directly modifies the maximum student capacity for a course using a <em>case-insensitive</em> match on the course code.
     *
     * @param courseId The course code to update (case-insensitive match)
     * @param maxCapacity The new maximum capacity to set (caller must ensure validity)
     * @return {@code true} if 1+ rows were updated (success), {@code false} if:
     * <ul>
     *   <li>No course matches the provided ID (case-insensitive comparison)</li>
     *   <li>New capacity is invalid (e.g., less than current enrollment)</li>
     *   <li>Database errors occur</li>
     * </ul>
     *
     * @implNote
     * <ul>
     *   <li>Uses <b>case-insensitive</b> matching for course code comparison</li>
     *   <li>Does NOT validate capacity against current enrollment - may set invalid values</li>
     *   <li>Uses parameterized queries to prevent SQL injection</li>
     *   <li>Caller should validate {@code maxCapacity ≥ current_enrollment ≥ 0}</li>
     *   <li>Errors are logged to stderr but not rethrown</li>
     * </ul>
     */
    public boolean updateCourseMaxCapacity(String courseId, int maxCapacity) {
        String updateSql = "UPDATE courses SET max_capacity = ? WHERE UPPER(course_code) = ?";

        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(updateSql)) {

            stmt.setInt(1, maxCapacity);
            stmt.setString(2, courseId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating course max capacity: " + e.getMessage());
            return false;
        }
    }
}