public class Course {
    private String courseCode;
    private String name;

    private int maxCapacity;
    private int currentEnrollment;
    private static int totalEnrolledStudents;

    public Course(String id, String name, int maxCapacity) {
        this.courseCode = id;
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.currentEnrollment = 0;
    }
    public String getId() {
        return courseCode;
    }
    public void setId(String courseCode) {
        this.courseCode = courseCode;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getMaxCapacity() {
        return maxCapacity;
    }
    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
    public int getCurrentEnrollment() {
        return currentEnrollment;
    }
    public static int getTotalEnrolledStudents() {
        return totalEnrolledStudents;
    }

    public boolean canEnroll() {
        return currentEnrollment < maxCapacity;
    }

    public void increaseEnrollment() {
        if (currentEnrollment < maxCapacity) {
            currentEnrollment++;
            totalEnrolledStudents++;
        }
    }

    @Override
    public String toString() {
        return getId() + " - " + getName() + ": Max Capacity - " + getMaxCapacity();
    }
}