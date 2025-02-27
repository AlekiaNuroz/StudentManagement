import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager {
    private final String databaseUrl = "jdbc:sqlite:university.db";
    private final String[] tableNames = {"students", "courses", "enrollments"};

    public DatabaseManager() {
        ensureTablesExist();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }

    private void ensureTablesExist() {
        for (String tableName : tableNames) {
            if (!doesTableExist(tableName)) {
                createTable(tableName);
            }
        }
    }

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

    private void createTable(String tableName) {
        String createTableSQL;
        switch (tableName) {
            case "students":
                createTableSQL = "CREATE TABLE IF NOT EXISTS students (" +
                        "id TEXT PRIMARY KEY, " +
                        "name TEXT NOT NULL)";
                break;
            case "courses":
                createTableSQL = "CREATE TABLE IF NOT EXISTS courses (" +
                        "course_code TEXT PRIMARY KEY, " +
                        "name TEXT NOT NULL, " +
                        "max_capacity INTEGER NOT NULL," +
                        "isDeleted BOOLEAN DEFAULT FALSE)";
                break;
            case "enrollments":
                createTableSQL = "CREATE TABLE IF NOT EXISTS enrollments (" +
                        "student_id TEXT, " +
                        "course_code TEXT, " +
                        "grade REAL, " +
                        "PRIMARY KEY (student_id, course_code), " +
                        "FOREIGN KEY(student_id) REFERENCES students(id), " +
                        "FOREIGN KEY(course_code) REFERENCES courses(course_code))";
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

    public Course getCourse(String id) {
        String sql = "SELECT * FROM courses WHERE UPPER(course_code) = ?";

        try (Connection conn = DriverManager.getConnection(databaseUrl);
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, id.toUpperCase());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return getCourseFromResultSet(resultSet); // Create and return Course object
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching course: " + e.getMessage());
        }
        return null;
    }

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
}