import java.sql.*;
import java.util.ArrayList;

/**
 * Manages database operations for the student management system,
 * ensuring tables exist and handling course-related transactions.
 */
public class DatabaseManager {
    private final String databaseUrl = "jdbc:sqlite:university.db";
    private final String[] tableNames = {"students", "courses", "enrollments"};

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
        return DriverManager.getConnection(databaseUrl);
    }

    /**
     * Ensures necessary tables exist by checking and creating them if absent.
     */
    private void ensureTablesExist() {
        for (String tableName : tableNames) {
            if (!doesTableExist(tableName)) {
                createTable(tableName);
            }
        }
    }

    /**
     * Checks if a specific table exists in the database.
     *
     * @param tableName Name of the table to check
     * @return true if the table exists, false otherwise
     */
    private boolean doesTableExist(String tableName) {
        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.out.println("Error while checking if table exists: " + e.getMessage());
            return false;
        }
    }

    /**
     * Creates a specified table in the database if it does not already exist.
     *
     * @param tableName Name of the table to create
     */
    private void createTable(String tableName) {
        String createTableSQL;
        switch (tableName) {
            case "students":
                createTableSQL = "CREATE TABLE IF NOT EXISTS students ("
                        + "id TEXT PRIMARY KEY, "
                        + "name TEXT NOT NULL)";
                break;
            case "courses":
                createTableSQL = "CREATE TABLE IF NOT EXISTS courses ("
                        + "course_code TEXT PRIMARY KEY, "
                        + "name TEXT NOT NULL, "
                        + "max_capacity INTEGER NOT NULL,"
                        + "isDeleted BOOLEAN DEFAULT FALSE)";
                break;
            case "enrollments":
                createTableSQL = "CREATE TABLE IF NOT EXISTS enrollments ("
                        + "student_id TEXT, "
                        + "course_code TEXT, "
                        + "grade REAL, "
                        + "PRIMARY KEY (student_id, course_code), "
                        + "FOREIGN KEY(student_id) REFERENCES students(id), "
                        + "FOREIGN KEY(course_code) REFERENCES courses(course_code))";
                break;
            default:
                System.out.println("Unknown table: " + tableName);
                return;
        }

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
                statement.execute(createTableSQL);
                System.out.println("Table " + tableName + " created successfully.");
        } catch (SQLException e) {
            System.out.println("Error while creating table: " + e.getMessage());
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

        try (Connection connection = DriverManager.getConnection(databaseUrl);
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

        try (Connection connection = DriverManager.getConnection(databaseUrl);
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

        try (Connection conn = DriverManager.getConnection(databaseUrl);
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
}
