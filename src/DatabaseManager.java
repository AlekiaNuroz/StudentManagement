import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;
import java.util.ArrayList;

/**
 * Manages database operations for the student management system,
 * ensuring tables exist and handling course-related transactions.
 */
public class DatabaseManager {
    private static final String databaseUrl = "jdbc:sqlite:university.db";
    private static final String dbUser = System.getenv("PGSQL_USERNAME");
    private static final String dbPassword = System.getenv("PGSQL_PASSWORD");
    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource dataSource;

    static {
        config.setJdbcUrl(databaseUrl);
        config.setUsername(dbUser);
        config.setPassword(dbPassword);
        config.addDataSourceProperty("cachePrepStmts" , "true");
        config.addDataSourceProperty("prepStmtCacheSize" , "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit" , "2048");
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
     * Ensures necessary tables exist by checking and creating them if absent.
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
                    + "isDeleted BOOLEAN DEFAULT FALSE)",
    
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
            System.out.println("All required tables ensured.");
        } catch (SQLException e) {
            System.err.println("Error ensuring tables: " + e.getMessage());
        }
    }

    /**
     * Inserts a new course into the database.
     *
     * @param course Course object to insert
     * @return true if insertion was successful, false otherwise
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
     * Updates a course's deleted status (delete or restore).
     *
     * @param course Course object to modify
     * @param delete true to mark the course as deleted, false to restore
     * @return true if the update was successful, false otherwise
     */
    public boolean deleteRestoreCourse(Course course, boolean delete) {
        String updateSQL = "UPDATE courses SET isDeleted = ? WHERE course_code = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(updateSQL)) {

            statement.setBoolean(1, delete);
            statement.setString(2, course.getId());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("Course " + course.getId() + " does not exist in the database.");
                return false;
            }
            return true;

        } catch (SQLException e) {
            System.out.println("Error updating course status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a list of courses based on deletion status.
     *
     * @param deleted true to fetch deleted courses, false for active courses
     * @return List of Course objects
     */
    public ArrayList<Course> getCourses(boolean deleted) {
        String selectSQL = "SELECT * FROM courses WHERE isDeleted=?";
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
     * Retrieves a course from the database based on its course code.
     *
     * @param id Course code
     * @return Course object if found, null otherwise
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
            return new Course(id, name, capacity);
        } catch (SQLException e) {
            System.err.println("Error retrieving course data from ResultSet: " + e.getMessage());
            return null; // Return null if there's an issue retrieving data
        }
    }

    private Student getStudentFromResultSet (ResultSet resultSet) {
        try {
            String studentId = resultSet.getString("id");
            String name = resultSet.getString("name");
            return new Student(name, studentId);
        } catch (SQLException e) {
            System.err.println("Error retrieving student data from ResultSet: " + e.getMessage());
            return null;
        }
    }
}

