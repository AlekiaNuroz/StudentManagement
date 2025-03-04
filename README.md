# Student Management System

A comprehensive student management system developed in Java, designed to streamline the processes of student enrollment and course management.

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Environment Variables](#environment-variables)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Student Enrollment**: Easily add, update, and remove student records.
- **Course Management**: Efficiently manage course information and student enrollments.
- **Database Integration**: Utilizes PostgreSQL for robust and reliable data storage.

## Installation

To set up the Student Management System on your local machine, follow these steps:

1. **Clone the repository**:

   ```
   git clone https://github.com/AlekiaNuroz/StudentManagement.git
   ```

2. **Navigate to the project directory**:

   ```
   cd StudentManagement
   ```

3. **Set up the database**:
   - Ensure you have a PostgreSQL server running.
   - Create a new PostgreSQL database for the application.
   - Set the required environment variables (see [Environment Variables](#environment-variables)).

4. **Build the project**:
   - Ensure you have the Java Development Kit (JDK) installed.
   - Compile the project using your preferred Integrated Development Environment (IDE) or command-line tools.

## Usage

1. **Run the application**:
   - Execute the main class to start the application.

2. **Interact with the system**:
   - **Add Students**: Input student details to enroll them.
   - **Manage Courses**: Create and assign courses to students.
   - **View Records**: Display student and course information.

## Environment Variables

The following environment variables need to be set before running the application:

```
PGSQL_PASSWORD=<your_database_password>
PGSQL_USERNAME=<your_database_username>
PGSQL_DATABASE=jdbc:postgresql://<server>:<port>/<database_name>
```

Make sure these values are correctly configured to connect to your PostgreSQL database.

## Contributing

Contributions are welcome! To contribute:

1. **Fork the repository**.
2. **Create a new branch**:

   ```
   git checkout -b feature-name
   ```

3. **Commit your changes**:

   ```
   git commit -m 'Add feature'
   ```

4. **Push to the branch**:

   ```
   git push origin feature-name
   ```

5. **Submit a pull request**.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

