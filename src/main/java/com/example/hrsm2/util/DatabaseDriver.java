package com.example.hrsm2.util;

import com.example.hrsm2.model.Employee;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all direct database interactions using JDBC and SQLite.
 * Manages connection, table creation, and CRUD operations for various entities.
 */
public class DatabaseDriver {

    private static final String DB_URL = "jdbc:sqlite:hr_database.db"; // Database file name
    // Formatter for storing/retrieving LocalDate as TEXT in yyyy-MM-dd format
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // --- SQL Schema Definition ---
    private static final String CREATE_EMPLOYEE_TABLE = "CREATE TABLE IF NOT EXISTS Employee ("
            + "id TEXT PRIMARY KEY, " // UUID stored as TEXT
            + "first_name TEXT NOT NULL, "
            + "last_name TEXT NOT NULL, "
            + "email TEXT NOT NULL UNIQUE, " // Email must be unique
            + "phone TEXT, "
            + "hire_date TEXT, " // Stored as 'yyyy-MM-dd'
            + "department TEXT, "
            + "job_title TEXT, "
            + "salary REAL"
            + ");";

    // Define other table schemas (ensure Foreign Keys reference Employee(id) correctly as TEXT)
    private static final String CREATE_LEAVE_TABLE = "CREATE TABLE IF NOT EXISTS LeaveManagement ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "employee_id TEXT NOT NULL, " // Foreign Key referencing Employee ID (TEXT)
            + "start_date TEXT, "
            + "end_date TEXT, "
            + "days INTEGER, "
            + "reason TEXT, "
            + "status TEXT, " // e.g., 'Pending', 'Approved', 'Rejected'
            + "manager_comment TEXT, "
            + "FOREIGN KEY(employee_id) REFERENCES Employee(id) ON DELETE CASCADE" // Cascade delete if employee is removed
            + ");";

    private static final String CREATE_PAYROLL_TABLE = "CREATE TABLE IF NOT EXISTS PayrollProcessing ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "employee_id TEXT NOT NULL, " // Foreign Key referencing Employee ID (TEXT)
            + "start_date TEXT, " // Pay period start
            + "end_date TEXT, "   // Pay period end
            + "base_salary REAL, "
            + "overtime REAL, "
            + "bonus REAL, "
            + "tax REAL, "
            + "other_deductions REAL, "
            + "net_salary REAL, "
            + "status TEXT, " // e.g., 'Processed', 'Pending'
            + "FOREIGN KEY(employee_id) REFERENCES Employee(id) ON DELETE CASCADE"
            + ");";

    private static final String CREATE_EVALUATION_TABLE = "CREATE TABLE IF NOT EXISTS PerformanceEvaluations ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "employee_id TEXT NOT NULL, " // Foreign Key referencing Employee ID (TEXT)
            + "date TEXT, " // Date of evaluation
            + "rating INTEGER, " // e.g., 1-5 scale
            + "strengths TEXT, "
            + "areas_of_improvements TEXT, "
            + "comments TEXT, "
            + "reviewed_by TEXT, " // Reviewer's name or ID
            + "FOREIGN KEY(employee_id) REFERENCES Employee(id) ON DELETE CASCADE"
            + ");";

    private static final String CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS UserManagement ("
            + "username TEXT PRIMARY KEY, "
            + "full_name TEXT, "
            + "password TEXT, " // Hashed password
            + "role TEXT" // e.g., 'Admin', 'Manager', 'Employee'
            + ");";

    // --- SQL CRUD Statements for Employee ---
    private static final String INSERT_EMPLOYEE_SQL = "INSERT INTO Employee(id, first_name, last_name, email, phone, hire_date, department, job_title, salary) VALUES(?,?,?,?,?,?,?,?,?)";
    private static final String SELECT_ALL_EMPLOYEES_SQL = "SELECT * FROM Employee ORDER BY last_name, first_name"; // Added default sorting
    private static final String SELECT_EMPLOYEE_BY_ID_SQL = "SELECT * FROM Employee WHERE id = ?";
    private static final String UPDATE_EMPLOYEE_SQL = "UPDATE Employee SET first_name = ?, last_name = ?, email = ?, phone = ?, hire_date = ?, department = ?, job_title = ?, salary = ? WHERE id = ?";
    private static final String DELETE_EMPLOYEE_SQL = "DELETE FROM Employee WHERE id = ?";
    private static final String SEARCH_EMPLOYEES_SQL = "SELECT * FROM Employee WHERE "
            + "lower(first_name) LIKE ? OR "
            + "lower(last_name) LIKE ? OR "
            + "lower(email) LIKE ? OR "
            + "lower(department) LIKE ? OR "
            + "lower(job_title) LIKE ? "
            + "ORDER BY last_name, first_name"; // Consistent sorting

    // --- SQL CRUD Statements for other entities (examples) ---
    private static final String INSERT_USER_SQL = "INSERT INTO UserManagement(username, full_name, password, role) VALUES(?,?,?,?)";
    // Add other INSERT, UPDATE, DELETE, SELECT statements for Leave, Payroll, Evaluation as needed

    private Connection connection;

    /**
     * Constructor establishes the database connection and ensures tables exist.
     */
    public DatabaseDriver() {
        try {
            // Load the SQLite JDBC driver (optional for modern JDBC, but good practice)
            // Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Database connection established to " + DB_URL);
            createTableIfNotExists();
        } catch (SQLException e) {
            System.err.println("FATAL: Database connection error: " + e.getMessage());
            // In a real app, might throw exception or exit if DB is essential
        } /*catch (ClassNotFoundException e) {
            System.err.println("FATAL: SQLite JDBC Driver not found.");
        }*/
    }

    /**
     * Executes the CREATE TABLE statements if the tables do not already exist.
     */
    private void createTableIfNotExists() {
        if (connection == null) {
            System.err.println("Cannot create tables, no database connection.");
            return;
        }
        // Use try-with-resources for the Statement
        try (Statement stmt = connection.createStatement()) {
            // Drop table only if absolutely necessary during schema change development
            // System.out.println("DEBUG: Dropping existing Employee table for schema update...");
            // stmt.execute("DROP TABLE IF EXISTS Employee;"); // !! DELETES ALL EMPLOYEE DATA !!

            stmt.execute(CREATE_EMPLOYEE_TABLE);
            stmt.execute(CREATE_LEAVE_TABLE);       // Create other tables
            stmt.execute(CREATE_PAYROLL_TABLE);
            stmt.execute(CREATE_EVALUATION_TABLE);
            stmt.execute(CREATE_USER_TABLE);
            System.out.println("Database tables checked/created successfully.");

            // Optional: Add default user if table is newly created
            // addDefaultAdminUserIfNotExists();

        } catch (SQLException e) {
            System.err.println("Error creating/checking tables: " + e.getMessage());
            // Log stack trace for detailed debugging
            e.printStackTrace();
        }
    }

    // --- Employee CRUD Methods ---

    /**
     * Inserts a new employee record into the database.
     * Assumes the Employee object has a non-null, valid UUID assigned.
     *
     * @param employee The Employee object to insert.
     * @return true if insertion was successful, false otherwise.
     */
    public boolean insertEmployee(Employee employee) {
        if (connection == null || employee == null || employee.getId() == null || employee.getId().trim().isEmpty()) {
            System.err.println("Cannot insert employee: Invalid input or no DB connection.");
            return false;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_EMPLOYEE_SQL)) {
            pstmt.setString(1, employee.getId()); // Use the pre-generated UUID
            pstmt.setString(2, employee.getFirstName());
            pstmt.setString(3, employee.getLastName());
            pstmt.setString(4, employee.getEmail());
            pstmt.setString(5, employee.getPhone());
            // Format LocalDate to String or null
            pstmt.setString(6, (employee.getHireDate() != null) ? employee.getHireDate().format(DATE_FORMATTER) : null);
            pstmt.setString(7, employee.getDepartment());
            pstmt.setString(8, employee.getJobTitle());
            pstmt.setDouble(9, employee.getSalary());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Employee inserted successfully (ID: " + employee.getId() + ")");
                return true;
            } else {
                System.err.println("Employee insertion failed (ID: " + employee.getId() + "), no rows affected.");
                return false;
            }
        } catch (SQLException e) {
            // Check for specific constraint violations (like duplicate ID or email)
            if (e.getErrorCode() == 19 /* SQLite constraint violation code */ ) {
                System.err.println("Error inserting employee: Constraint violation (ID or Email likely exists): " + e.getMessage());
            } else {
                System.err.println("Error inserting employee (ID: " + employee.getId() + "): " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Retrieves all employee records from the database, ordered by name.
     *
     * @return A List of Employee objects, or an empty list if none found or error occurs.
     */
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        if (connection == null) {
            System.err.println("Cannot get employees: No DB connection.");
            return employees; // Return empty list
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_EMPLOYEES_SQL)) {

            while (rs.next()) {
                employees.add(mapResultSetToEmployee(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving employees: " + e.getMessage());
            e.printStackTrace();
        }
        return employees;
    }

    /**
     * Retrieves a single employee by their String ID (UUID).
     *
     * @param id The String UUID of the employee to find.
     * @return The Employee object if found, otherwise null.
     */
    public Employee getEmployeeById(String id) {
        if (connection == null || id == null || id.trim().isEmpty()) {
            System.err.println("Cannot get employee by ID: Invalid input or no DB connection.");
            return null;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_EMPLOYEE_BY_ID_SQL)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                } else {
                    System.out.println("Employee not found with ID: " + id);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving employee by ID (" + id + "): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Updates an existing employee record in the database.
     *
     * @param employee The Employee object containing updated data (must have correct ID).
     * @return true if the update was successful (at least one row affected), false otherwise.
     */
    public boolean updateEmployee(Employee employee) {
        if (connection == null || employee == null || employee.getId() == null || employee.getId().trim().isEmpty()) {
            System.err.println("Cannot update employee: Invalid input or no DB connection.");
            return false;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(UPDATE_EMPLOYEE_SQL)) {
            pstmt.setString(1, employee.getFirstName());
            pstmt.setString(2, employee.getLastName());
            pstmt.setString(3, employee.getEmail());
            pstmt.setString(4, employee.getPhone());
            pstmt.setString(5, (employee.getHireDate() != null) ? employee.getHireDate().format(DATE_FORMATTER) : null);
            pstmt.setString(6, employee.getDepartment());
            pstmt.setString(7, employee.getJobTitle());
            pstmt.setDouble(8, employee.getSalary());
            pstmt.setString(9, employee.getId()); // WHERE clause uses the String ID

            int affectedRows = pstmt.executeUpdate();
            if(affectedRows > 0) {
                System.out.println("Employee updated successfully (ID: " + employee.getId() + ")");
                return true;
            } else {
                // This can happen if the ID doesn't exist
                System.out.println("Employee not found or no changes made during update (ID: " + employee.getId() + ")");
                return false;
            }
        } catch (SQLException e) {
            // Check for potential unique constraint violation (e.g., changing email to one that already exists)
            if (e.getErrorCode() == 19 && e.getMessage().toLowerCase().contains("employee.email")) {
                System.err.println("Error updating employee: Email constraint violation: " + e.getMessage());
            } else {
                System.err.println("Error updating employee (ID: " + employee.getId() + "): " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Deletes an employee record from the database using their String ID (UUID).
     *
     * @param id The String UUID of the employee to delete.
     * @return true if the deletion was successful (at least one row affected), false otherwise.
     */
    public boolean deleteEmployee(String id) {
        if (connection == null || id == null || id.trim().isEmpty()) {
            System.err.println("Cannot delete employee: Invalid ID or no DB connection.");
            return false;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(DELETE_EMPLOYEE_SQL)) {
            pstmt.setString(1, id); // Use String ID in WHERE clause
            int affectedRows = pstmt.executeUpdate();
            if(affectedRows > 0) {
                System.out.println("Employee deleted successfully (ID: " + id + ")");
                return true;
            } else {
                // This can happen if the ID doesn't exist
                System.out.println("Employee not found for deletion (ID: " + id + ")");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting employee (ID: " + id + "): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Searches for employees based on a keyword matching first name, last name,
     * email, department, or job title (case-insensitive).
     *
     * @param keyword The search term.
     * @return A List of matching Employee objects, ordered by name.
     */
    public List<Employee> searchEmployees(String keyword) {
        List<Employee> employees = new ArrayList<>();
        if (connection == null) {
            System.err.println("Cannot search employees: No DB connection.");
            return employees;
        }
        // Handle null or empty keyword - return all employees in this case
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEmployees();
        }

        String searchTerm = "%" + keyword.toLowerCase() + "%"; // Prepare for LIKE query

        try (PreparedStatement pstmt = connection.prepareStatement(SEARCH_EMPLOYEES_SQL)) {
            pstmt.setString(1, searchTerm); // first_name
            pstmt.setString(2, searchTerm); // last_name
            pstmt.setString(3, searchTerm); // email
            pstmt.setString(4, searchTerm); // department
            pstmt.setString(5, searchTerm); // job_title

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(mapResultSetToEmployee(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching employees with keyword '" + keyword + "': " + e.getMessage());
            e.printStackTrace();
        }
        return employees;
    }

    /**
     * Helper method to map a row from a ResultSet to an Employee object.
     *
     * @param rs The ResultSet cursor, positioned at the row to map.
     * @return An Employee object populated with data from the current row.
     * @throws SQLException If a database access error occurs.
     */
    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        LocalDate hireDate = null;
        String hireDateStr = rs.getString("hire_date");
        if (hireDateStr != null && !hireDateStr.isEmpty()) {
            try {
                // Parse the date string using the defined formatter
                hireDate = LocalDate.parse(hireDateStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                // Log error if parsing fails, but don't stop processing other data
                System.err.println("Warning: Could not parse hire date '" + hireDateStr + "' for employee ID " + id);
            }
        }
        String department = rs.getString("department");
        String jobTitle = rs.getString("job_title");
        double salary = rs.getDouble("salary");

        // Use the constructor that accepts the String ID
        return new Employee(id, firstName, lastName, email, phone, hireDate, department, jobTitle, salary);
    }


    // --- User Management Methods ---

    /**
     * Hashes a password using SHA-256.
     * @param password The plain text password.
     * @return The hex string representation of the hashed password.
     * @throws NoSuchAlgorithmException If SHA-256 is not available.
     */
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Inserts a new user with a hashed password.
     *
     * @param username User's unique username.
     * @param fullName User's full name.
     * @param password User's plain text password (will be hashed).
     * @param role User's role (e.g., 'Admin').
     * @return true if successful, false otherwise.
     */
    public boolean insertUser(String username, String fullName, String password, String role) {
        if (connection == null || username == null || password == null || role == null) return false;
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_USER_SQL)) {
            pstmt.setString(1, username);
            pstmt.setString(2, fullName);
            pstmt.setString(3, hashPassword(password)); // Hash the password
            pstmt.setString(4, role);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("User '" + username + "' inserted successfully.");
                return true;
            }
            return false;
        } catch (SQLException | NoSuchAlgorithmException e) {
            System.err.println("Error inserting user '" + username + "': " + e.getMessage());
            if (e instanceof SQLException && ((SQLException)e).getErrorCode() == 19) {
                System.err.println("Hint: Username might already exist.");
            }
            return false;
        }
    }

    /**
     * Closes the database connection. Should be called when the application shuts down.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}