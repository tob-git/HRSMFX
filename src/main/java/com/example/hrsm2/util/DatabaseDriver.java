package com.example.hrsm2.util;

import com.example.hrsm2.model.Employee;
import com.example.hrsm2.model.LeaveRequest;
import com.example.hrsm2.model.Payroll;
import com.example.hrsm2.model.User;
import com.example.hrsm2.model.PerformanceEvaluation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Field;

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

    private static final String CREATE_LEAVE_TABLE = "CREATE TABLE IF NOT EXISTS LeaveManagement ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, " // Auto-increment integer ID
            + "employee_id TEXT NOT NULL, " // Foreign Key referencing Employee ID (TEXT)
            + "start_date TEXT NOT NULL, "    // Stored as 'yyyy-MM-dd'
            + "end_date TEXT NOT NULL, "      // Stored as 'yyyy-MM-dd'
            + "reason TEXT, "
            + "status TEXT NOT NULL, " // e.g., 'PENDING', 'APPROVED', 'REJECTED'
            + "manager_comments TEXT, "
            + "FOREIGN KEY(employee_id) REFERENCES Employee(id) ON DELETE CASCADE" // Cascade delete if employee is removed
            + ");";

    private static final String CREATE_PAYROLL_TABLE = "CREATE TABLE IF NOT EXISTS Payroll ("
            + "id TEXT PRIMARY KEY, "                  // UUID stored as TEXT
            + "employee_id TEXT NOT NULL, "           // Foreign Key referencing Employee ID (TEXT)
            + "pay_period_start TEXT NOT NULL, "     // Stored as 'yyyy-MM-dd'
            + "pay_period_end TEXT NOT NULL, "       // Stored as 'yyyy-MM-dd'
            + "base_salary REAL DEFAULT 0.0, "
            + "overtime_pay REAL DEFAULT 0.0, "
            + "bonus REAL DEFAULT 0.0, "
            + "tax_deductions REAL DEFAULT 0.0, "
            + "other_deductions REAL DEFAULT 0.0, "
            + "net_salary REAL DEFAULT 0.0, "
            + "status TEXT NOT NULL, "                // e.g., 'PENDING', 'PROCESSED', 'PAID'
            + "FOREIGN KEY(employee_id) REFERENCES Employee(id) ON DELETE CASCADE"
            + ");";


    private static final String CREATE_EVALUATION_TABLE = "CREATE TABLE IF NOT EXISTS PerformanceEvaluations ("
            + "id TEXT PRIMARY KEY, "                  // Store UUID as TEXT
            + "employee_id TEXT NOT NULL, "           // Foreign Key referencing Employee ID (TEXT)
            + "evaluation_date TEXT NOT NULL, "       // Stored as 'yyyy-MM-dd'
            + "rating INTEGER, "                      // e.g., 1-5 scale
            + "strengths TEXT, "
            + "areas_for_improvement TEXT, "
            + "comments TEXT, "
            + "reviewed_by TEXT, "                    // Reviewer's name or ID
            + "FOREIGN KEY(employee_id) REFERENCES Employee(id) ON DELETE CASCADE" // Cascade delete
            + ");";

    private static final String CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS UserManagement ("
            + "username TEXT PRIMARY KEY, "
            + "full_name TEXT NOT NULL, "
            + "password TEXT NOT NULL, " // Hashed password
            + "role TEXT NOT NULL" // e.g., 'SUPER_ADMIN', 'HR_ADMIN'
            + ");";

    // --- SQL CRUD Statements for Employee ---
    private static final String INSERT_EMPLOYEE_SQL = "INSERT INTO Employee(id, first_name, last_name, email, phone, hire_date, department, job_title, salary) VALUES(?,?,?,?,?,?,?,?,?)";
    private static final String SELECT_ALL_EMPLOYEES_SQL = "SELECT * FROM Employee ORDER BY last_name, first_name";
    private static final String SELECT_EMPLOYEE_BY_ID_SQL = "SELECT * FROM Employee WHERE id = ?";
    private static final String UPDATE_EMPLOYEE_SQL = "UPDATE Employee SET first_name = ?, last_name = ?, email = ?, phone = ?, hire_date = ?, department = ?, job_title = ?, salary = ? WHERE id = ?";
    private static final String DELETE_EMPLOYEE_SQL = "DELETE FROM Employee WHERE id = ?";
    private static final String SEARCH_EMPLOYEES_SQL = "SELECT * FROM Employee WHERE "
            + "lower(first_name) LIKE ? OR "
            + "lower(last_name) LIKE ? OR "
            + "lower(email) LIKE ? OR "
            + "lower(department) LIKE ? OR "
            + "lower(job_title) LIKE ? "
            + "ORDER BY last_name, first_name";

    // --- SQL CRUD Statements for LeaveManagement ---
    private static final String INSERT_LEAVE_SQL = "INSERT INTO LeaveManagement(employee_id, start_date, end_date, reason, status, manager_comments) VALUES(?,?,?,?,?,?)";
    private static final String SELECT_ALL_LEAVES_SQL = "SELECT * FROM LeaveManagement ORDER BY start_date DESC";
    private static final String SELECT_LEAVE_BY_ID_SQL = "SELECT * FROM LeaveManagement WHERE id = ?";
    private static final String SELECT_LEAVES_BY_EMPLOYEE_ID_SQL = "SELECT * FROM LeaveManagement WHERE employee_id = ? ORDER BY start_date DESC";
    private static final String SELECT_APPROVED_LEAVES_BY_EMPLOYEE_ID_SQL = "SELECT * FROM LeaveManagement WHERE employee_id = ? AND status = 'APPROVED'";
    private static final String UPDATE_LEAVE_SQL = "UPDATE LeaveManagement SET employee_id = ?, start_date = ?, end_date = ?, reason = ?, status = ?, manager_comments = ? WHERE id = ?";
    private static final String DELETE_LEAVE_SQL = "DELETE FROM LeaveManagement WHERE id = ?";

    // --- SQL CRUD Statements for User ---
    private static final String INSERT_USER_SQL = "INSERT INTO UserManagement(username, full_name, password, role) VALUES(?,?,?,?)";
    private static final String SELECT_ALL_USERS_SQL = "SELECT * FROM UserManagement ORDER BY full_name";
    private static final String SELECT_USER_BY_USERNAME_SQL = "SELECT * FROM UserManagement WHERE username = ?";
    private static final String UPDATE_USER_SQL = "UPDATE UserManagement SET full_name = ?, password = ?, role = ? WHERE username = ?";
    private static final String DELETE_USER_SQL = "DELETE FROM UserManagement WHERE username = ?";

    // --- SQL CRUD Statements for Payroll ---
    private static final String INSERT_PAYROLL_SQL = "INSERT INTO Payroll("
            + "id, employee_id, pay_period_start, pay_period_end, base_salary, "
            + "overtime_pay, bonus, tax_deductions, other_deductions, net_salary, status) "
            + "VALUES(?,?,?,?,?,?,?,?,?,?,?)";
    private static final String SELECT_ALL_PAYROLLS_SQL = "SELECT * FROM Payroll ORDER BY pay_period_start DESC, employee_id";
    private static final String SELECT_PAYROLL_BY_ID_SQL = "SELECT * FROM Payroll WHERE id = ?";
    private static final String SELECT_PAYROLLS_BY_EMPLOYEE_ID_SQL = "SELECT * FROM Payroll WHERE employee_id = ? ORDER BY pay_period_start DESC";
    private static final String UPDATE_PAYROLL_SQL = "UPDATE Payroll SET "
            + "employee_id = ?, pay_period_start = ?, pay_period_end = ?, base_salary = ?, "
            + "overtime_pay = ?, bonus = ?, tax_deductions = ?, other_deductions = ?, "
            + "net_salary = ?, status = ? WHERE id = ?";
    private static final String DELETE_PAYROLL_SQL = "DELETE FROM Payroll WHERE id = ?";

    // --- SQL CRUD Statements for PerformanceEvaluation ---
    private static final String INSERT_EVALUATION_SQL = "INSERT INTO PerformanceEvaluations("
            + "id, employee_id, evaluation_date, rating, strengths, "
            + "areas_for_improvement, comments, reviewed_by) "
            + "VALUES(?,?,?,?,?,?,?,?)";
    private static final String SELECT_ALL_EVALUATIONS_SQL = "SELECT * FROM PerformanceEvaluations ORDER BY evaluation_date DESC, employee_id";
    private static final String SELECT_EVALUATION_BY_ID_SQL = "SELECT * FROM PerformanceEvaluations WHERE id = ?";
    private static final String SELECT_EVALUATIONS_BY_EMPLOYEE_ID_SQL = "SELECT * FROM PerformanceEvaluations WHERE employee_id = ? ORDER BY evaluation_date DESC";
    private static final String UPDATE_EVALUATION_SQL = "UPDATE PerformanceEvaluations SET "
            + "employee_id = ?, evaluation_date = ?, rating = ?, strengths = ?, "
            + "areas_for_improvement = ?, comments = ?, reviewed_by = ? WHERE id = ?";
    private static final String DELETE_EVALUATION_SQL = "DELETE FROM PerformanceEvaluations WHERE id = ?";

    private Connection connection;

    // --- Singleton Pattern ---
    private static DatabaseDriver instance;

    /**
     * Private constructor for Singleton pattern.
     * Establishes the database connection and ensures tables exist.
     */
    public DatabaseDriver() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTableIfNotExists();
            // Ensure the super admin exists on first run or subsequent startups
            ensureSuperAdminExists();
        } catch (SQLException e) {
            // Error handled by caller or application logic
        }
    }

    /**
     * Gets the single instance of DatabaseDriver.
     *
     * @return The singleton DatabaseDriver instance.
     */
    public static synchronized DatabaseDriver getInstance() {
        if (instance == null) {
            instance = new DatabaseDriver();
        }
        return instance;
    }

    // --- Table Creation ---
    private void createTableIfNotExists() {
        if (connection == null) {
            return;
        }
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(CREATE_EMPLOYEE_TABLE);
            stmt.execute(CREATE_LEAVE_TABLE);
            stmt.execute(CREATE_PAYROLL_TABLE);
            stmt.execute(CREATE_EVALUATION_TABLE);
            stmt.execute(CREATE_USER_TABLE);
        } catch (SQLException e) {
            // Error handled by caller or application logic
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
            return affectedRows > 0;

        } catch (SQLException e) {
            // Check for specific constraint violations (like duplicate ID or email)
            // if (e.getErrorCode() == 19 /* SQLite constraint violation code */ ) { ... }
            // Error handled by caller or application logic
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
            return employees; // Return empty list
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_EMPLOYEES_SQL)) {

            while (rs.next()) {
                employees.add(mapResultSetToEmployee(rs));
            }
        } catch (SQLException e) {
            // Error handled by caller or application logic
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
            return null;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_EMPLOYEE_BY_ID_SQL)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                } else {
                    return null; // Not found
                }
            }
        } catch (SQLException e) {
            // Error handled by caller or application logic
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
            return affectedRows > 0;

        } catch (SQLException e) {
            // Check for potential unique constraint violation (e.g., changing email to one that already exists)
            // if (e.getErrorCode() == 19 && e.getMessage().toLowerCase().contains("employee.email")) { ... }
            // Error handled by caller or application logic
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
            return false;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(DELETE_EMPLOYEE_SQL)) {
            pstmt.setString(1, id); // Use String ID in WHERE clause
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            // Error handled by caller or application logic
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
            // Error handled by caller or application logic
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
        LocalDate hireDate = parseDate(rs.getString("hire_date")); // Use helper
        String department = rs.getString("department");
        String jobTitle = rs.getString("job_title");
        double salary = rs.getDouble("salary");

        // Use the constructor that accepts the String ID
        return new Employee(id, firstName, lastName, email, phone, hireDate, department, jobTitle, salary);
    }


    // --- Leave Request CRUD Methods ---

    /**
     * Inserts a new leave request record into the database.
     *
     * @param leaveRequest The LeaveRequest object to insert (ID should be null or 0).
     * @return The generated ID of the inserted request, or -1 if insertion failed.
     */
    public int insertLeaveRequest(LeaveRequest leaveRequest) {
        if (connection == null || leaveRequest == null) {
            return -1;
        }
        // ID should be null or 0 as it's auto-generated

        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_LEAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, leaveRequest.getEmployeeId());
            pstmt.setString(2, leaveRequest.getStartDate() != null ? leaveRequest.getStartDate().format(DATE_FORMATTER) : null);
            pstmt.setString(3, leaveRequest.getEndDate() != null ? leaveRequest.getEndDate().format(DATE_FORMATTER) : null);
            pstmt.setString(4, leaveRequest.getReason());
            pstmt.setString(5, leaveRequest.getStatus().name()); // Convert enum to string
            pstmt.setString(6, leaveRequest.getManagerComments());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Return the auto-generated ID
                    } else {
                        return -1; // Indicate an issue retrieving the ID
                    }
                }
            } else {
                return -1;
            }
        } catch (SQLException e) {
            // Error handled by caller or application logic
            return -1;
        }
    }

    /**
     * Retrieves all leave request records from the database.
     *
     * @return A List of LeaveRequest objects, or an empty list if none found or error occurs.
     */
    public List<LeaveRequest> getAllLeaveRequests() {
        List<LeaveRequest> requests = new ArrayList<>();
        if (connection == null) {
            return requests;
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_LEAVES_SQL)) {

            while (rs.next()) {
                requests.add(mapResultSetToLeaveRequest(rs));
            }
        } catch (SQLException e) {
            // Error handled by caller or application logic
        }
        return requests;
    }

    /**
     * Retrieves a single leave request by its Integer ID.
     *
     * @param id The Integer ID of the leave request.
     * @return The LeaveRequest object if found, otherwise null.
     */
    public LeaveRequest getLeaveRequestById(int id) {
        if (connection == null || id <= 0) {
            return null;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_LEAVE_BY_ID_SQL)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLeaveRequest(rs);
                } else {
                    return null; // Not found
                }
            }
        } catch (SQLException e) {
            // Error handled by caller or application logic
            return null;
        }
    }

    /**
     * Retrieves all leave requests for a specific employee.
     *
     * @param employeeId The String UUID of the employee.
     * @return A List of LeaveRequest objects for the employee, or an empty list.
     */
    public List<LeaveRequest> getLeaveRequestsByEmployeeId(String employeeId) {
        List<LeaveRequest> requests = new ArrayList<>();
        if (connection == null || employeeId == null || employeeId.trim().isEmpty()) {
            return requests;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_LEAVES_BY_EMPLOYEE_ID_SQL)) {
            pstmt.setString(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapResultSetToLeaveRequest(rs));
                }
            }
        } catch (SQLException e) {
            // Error handled by caller or application logic
        }
        return requests;
    }

    /**
     * Retrieves all *approved* leave requests for a specific employee.
     * Used for calculating used leave days.
     *
     * @param employeeId The String UUID of the employee.
     * @return A List of approved LeaveRequest objects for the employee, or an empty list.
     */
    public List<LeaveRequest> getApprovedLeaveRequestsByEmployeeId(String employeeId) {
        List<LeaveRequest> requests = new ArrayList<>();
        if (connection == null || employeeId == null || employeeId.trim().isEmpty()) {
            return requests;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_APPROVED_LEAVES_BY_EMPLOYEE_ID_SQL)) {
            pstmt.setString(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapResultSetToLeaveRequest(rs));
                }
            }
        } catch (SQLException e) {
            // Error handled by caller or application logic
        }
        return requests;
    }


    /**
     * Updates an existing leave request record in the database.
     *
     * @param leaveRequest The LeaveRequest object containing updated data (must have correct Integer ID).
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateLeaveRequest(LeaveRequest leaveRequest) {
        if (connection == null || leaveRequest == null || leaveRequest.getId() == null || leaveRequest.getId() <= 0) {
            return false;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(UPDATE_LEAVE_SQL)) {
            pstmt.setString(1, leaveRequest.getEmployeeId());
            pstmt.setString(2, leaveRequest.getStartDate() != null ? leaveRequest.getStartDate().format(DATE_FORMATTER) : null);
            pstmt.setString(3, leaveRequest.getEndDate() != null ? leaveRequest.getEndDate().format(DATE_FORMATTER) : null);
            pstmt.setString(4, leaveRequest.getReason());
            pstmt.setString(5, leaveRequest.getStatus().name()); // Enum to string
            pstmt.setString(6, leaveRequest.getManagerComments());
            pstmt.setInt(7, leaveRequest.getId()); // WHERE clause uses the Integer ID

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            // Error handled by caller or application logic
            return false;
        }
    }

    /**
     * Deletes a leave request record from the database using its Integer ID.
     *
     * @param id The Integer ID of the leave request to delete.
     * @return true if deletion was successful, false otherwise.
     */
    public boolean deleteLeaveRequest(int id) {
        if (connection == null || id <= 0) {
            return false;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(DELETE_LEAVE_SQL)) {
            pstmt.setInt(1, id); // Use Integer ID
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            // Error handled by caller or application logic
            return false;
        }
    }

    /**
     * Helper method to map a row from a ResultSet to a LeaveRequest object.
     *
     * @param rs The ResultSet cursor, positioned at the row to map.
     * @return A LeaveRequest object populated with data.
     * @throws SQLException If a database access error occurs.
     */
    private LeaveRequest mapResultSetToLeaveRequest(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String employeeId = rs.getString("employee_id");
        LocalDate startDate = parseDate(rs.getString("start_date"));
        LocalDate endDate = parseDate(rs.getString("end_date"));
        String reason = rs.getString("reason");
        String statusStr = rs.getString("status");
        String managerComments = rs.getString("manager_comments");

        LeaveRequest.LeaveStatus status = LeaveRequest.LeaveStatus.PENDING; // Default
        try {
            if (statusStr != null && !statusStr.isEmpty()) {
                status = LeaveRequest.LeaveStatus.valueOf(statusStr.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            // Log or handle invalid status from DB if necessary, defaulting is usually safe
        }

        // Use the constructor that includes the Integer ID
        return new LeaveRequest(id, employeeId, startDate, endDate, reason, status, managerComments);
    }

    /**
     * Safely parses a date string using the predefined formatter.
     * @param dateStr The date string (e.g., "yyyy-MM-dd") or null.
     * @return The LocalDate object or null if input is null, empty, or invalid format.
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null; // Return null on parse error
        }
    }


    // --- User Management Methods ---

    /**
     * Hashes a password using SHA-256.
     * Public now so UserService can use it for comparison during authentication.
     * @param password The plain text password.
     * @return The hex string representation of the hashed password, or null if error.
     */
    public String hashPassword(String password) {
        if (password == null) return null;
        try {
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
        } catch (NoSuchAlgorithmException e) {
            return null; // Indicate failure
        }
    }

    /**
     * Inserts a new user with a hashed password.
     *
     * @param username User's unique username.
     * @param fullName User's full name.
     * @param plainPassword User's plain text password (will be hashed).
     * @param role User's role as a String (e.g., "SUPER_ADMIN", "HR_ADMIN").
     * @return true if successful, false otherwise.
     */
    public boolean insertUser(String username, String fullName, String plainPassword, String role) {
        if (connection == null || username == null || username.trim().isEmpty() ||
                fullName == null || fullName.trim().isEmpty() || plainPassword == null ||
                role == null || role.trim().isEmpty()) {
            return false;
        }

        String hashedPassword = hashPassword(plainPassword);
        if (hashedPassword == null) {
            return false; // Hashing failed
        }

        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_USER_SQL)) {
            pstmt.setString(1, username.trim());
            pstmt.setString(2, fullName.trim());
            pstmt.setString(3, hashedPassword);
            pstmt.setString(4, role.trim().toUpperCase()); // Store role consistently as uppercase string
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            // Check for primary key violation (username exists)
            // if (e.getErrorCode() == 19 /* SQLite constraint violation */ || ...) { ... }
            // Error handled by caller or application logic
            return false;
        }
    }

    /**
     * Retrieves a single user by their username.
     *
     * @param username The username to search for.
     * @return The User object if found, otherwise null.
     */
    public User getUserByUsername(String username) {
        if (connection == null || username == null || username.trim().isEmpty()) {
            return null;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_USER_BY_USERNAME_SQL)) {
            pstmt.setString(1, username.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                } else {
                    return null; // User not found
                }
            }
        } catch (SQLException e) {
            // Error handled by caller or application logic
            return null;
        }
    }

    /**
     * Retrieves all users from the database.
     *
     * @return A List of User objects, or an empty list if none found or error occurs.
     */
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        if (connection == null) {
            return userList; // Return empty list
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_USERS_SQL)) {

            while (rs.next()) {
                userList.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            // Error handled by caller or application logic
        }
        return userList;
    }

    /**
     * Updates an existing user's details (full name, password hash, role).
     * Note: The password in the User object MUST be the HASHED password.
     *
     * @param user The User object containing the updated data (username identifies the user).
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateUser(User user) {
        if (connection == null || user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return false;
        }
        // We assume user.getPassword() contains the CORRECT HASH here
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return false;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(UPDATE_USER_SQL)) {
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getPassword()); // Assumes this is already hashed
            pstmt.setString(3, user.getRole().name()); // Convert enum to string (e.g., "SUPER_ADMIN")
            pstmt.setString(4, user.getUsername());   // WHERE clause

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            // Error handled by caller or application logic
            return false;
        }
    }


    /**
     * Deletes a user from the database by username.
     * Does not allow deleting the 'super' admin user.
     *
     * @param username The username of the user to delete.
     * @return true if deletion was successful, false otherwise.
     */
    public boolean deleteUser(String username) {
        if (connection == null || username == null || username.trim().isEmpty()) {
            return false;
        }

        // Prevent deleting the primary super admin account
        if ("super".equalsIgnoreCase(username.trim())) {
            return false;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(DELETE_USER_SQL)) {
            pstmt.setString(1, username.trim());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            // Error handled by caller or application logic
            return false;
        }
    }

    /**
     * Helper method to map a ResultSet row to a User object.
     *
     * @param rs ResultSet positioned at the correct row.
     * @return A User object.
     * @throws SQLException If database error occurs.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        String username = rs.getString("username");
        String fullName = rs.getString("full_name");
        String hashedPassword = rs.getString("password");
        String roleStr = rs.getString("role");

        User.UserRole role = User.UserRole.HR_ADMIN; // Default role if parsing fails
        try {
            if (roleStr != null && !roleStr.isEmpty()) {
                // Convert the stored string (e.g., "SUPER_ADMIN") back to the enum constant
                role = User.UserRole.valueOf(roleStr.trim().toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            // Log or handle invalid role from DB if necessary
        }

        // Create user object using the constructor that takes username, HASHED password, full name, and role.
        return new User(username, hashedPassword, fullName, role);
    }

    /**
     * Inserts a new payroll record into the database.
     * Assumes the Payroll object has a non-null, valid UUID assigned.
     *
     * @param payroll The Payroll object to insert.
     * @return true if insertion was successful, false otherwise.
     */
    public boolean insertPayroll(Payroll payroll) {
        if (connection == null || payroll == null || payroll.getId() == null || payroll.getId().trim().isEmpty()) {
            return false;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_PAYROLL_SQL)) {
            pstmt.setString(1, payroll.getId());
            pstmt.setString(2, payroll.getEmployeeId());
            pstmt.setString(3, payroll.getPayPeriodStart() != null ? payroll.getPayPeriodStart().format(DATE_FORMATTER) : null);
            pstmt.setString(4, payroll.getPayPeriodEnd() != null ? payroll.getPayPeriodEnd().format(DATE_FORMATTER) : null);
            pstmt.setDouble(5, payroll.getBaseSalary());
            pstmt.setDouble(6, payroll.getOvertimePay());
            pstmt.setDouble(7, payroll.getBonus());
            pstmt.setDouble(8, payroll.getTaxDeductions());
            pstmt.setDouble(9, payroll.getOtherDeductions());
            pstmt.setDouble(10, payroll.getNetSalary());
            pstmt.setString(11, payroll.getStatus().name()); // Enum to String

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            // if (e.getErrorCode() == 19) { ... } // Constraint violation
            // Error handled by caller or application logic
            return false;
        }
    }

    /**
     * Retrieves all payroll records from the database.
     *
     * @return A List of Payroll objects, or an empty list if none found or error occurs.
     */
    public List<Payroll> getAllPayrolls() {
        List<Payroll> payrollList = new ArrayList<>();
        if (connection == null) {
            return payrollList;
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_PAYROLLS_SQL)) {
            while (rs.next()) {
                payrollList.add(mapResultSetToPayroll(rs));
            }
        } catch (SQLException | ReflectiveOperationException e) {
            // Error handled by caller or application logic (including reflection error)
        }
        return payrollList;
    }

    /**
     * Retrieves a single payroll record by its String ID (UUID).
     *
     * @param id The String UUID of the payroll record.
     * @return The Payroll object if found, otherwise null.
     */
    public Payroll getPayrollById(String id) {
        if (connection == null || id == null || id.trim().isEmpty()) {
            return null;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_PAYROLL_BY_ID_SQL)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPayroll(rs);
                } else {
                    return null; // Not found
                }
            }
        } catch (SQLException | ReflectiveOperationException e) {
            // Error handled by caller or application logic
            return null;
        }
    }

    /**
     * Retrieves all payroll records for a specific employee.
     *
     * @param employeeId The String UUID of the employee.
     * @return A List of Payroll objects for the employee, or an empty list.
     */
    public List<Payroll> getPayrollsByEmployeeId(String employeeId) {
        List<Payroll> payrollList = new ArrayList<>();
        if (connection == null || employeeId == null || employeeId.trim().isEmpty()) {
            return payrollList;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_PAYROLLS_BY_EMPLOYEE_ID_SQL)) {
            pstmt.setString(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    payrollList.add(mapResultSetToPayroll(rs));
                }
            }
        } catch (SQLException | ReflectiveOperationException e) {
            // Error handled by caller or application logic
        }
        return payrollList;
    }

    /**
     * Updates an existing payroll record in the database.
     *
     * @param payroll The Payroll object containing updated data (must have correct ID).
     * @return true if the update was successful, false otherwise.
     */
    public boolean updatePayroll(Payroll payroll) {
        if (connection == null || payroll == null || payroll.getId() == null || payroll.getId().trim().isEmpty()) {
            return false;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(UPDATE_PAYROLL_SQL)) {
            pstmt.setString(1, payroll.getEmployeeId());
            pstmt.setString(2, payroll.getPayPeriodStart() != null ? payroll.getPayPeriodStart().format(DATE_FORMATTER) : null);
            pstmt.setString(3, payroll.getPayPeriodEnd() != null ? payroll.getPayPeriodEnd().format(DATE_FORMATTER) : null);
            pstmt.setDouble(4, payroll.getBaseSalary());
            pstmt.setDouble(5, payroll.getOvertimePay());
            pstmt.setDouble(6, payroll.getBonus());
            pstmt.setDouble(7, payroll.getTaxDeductions());
            pstmt.setDouble(8, payroll.getOtherDeductions());
            pstmt.setDouble(9, payroll.getNetSalary());
            pstmt.setString(10, payroll.getStatus().name()); // Enum to String
            pstmt.setString(11, payroll.getId()); // WHERE clause

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            // Error handled by caller or application logic
            return false;
        }
    }

    /**
     * Deletes a payroll record from the database using its String ID (UUID).
     *
     * @param id The String UUID of the payroll record to delete.
     * @return true if deletion was successful, false otherwise.
     */
    public boolean deletePayroll(String id) {
        if (connection == null || id == null || id.trim().isEmpty()) {
            return false;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(DELETE_PAYROLL_SQL)) {
            pstmt.setString(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            // Error handled by caller or application logic
            return false;
        }
    }

    /**
     * Helper method to map a row from a ResultSet to a Payroll object.
     * Uses reflection to set the netSalary field directly from the database value.
     *
     * @param rs The ResultSet cursor, positioned at the row to map.
     * @return A Payroll object populated with data.
     * @throws SQLException If a database access error occurs.
     * @throws ReflectiveOperationException If reflection fails to set netSalary.
     */
    private Payroll mapResultSetToPayroll(ResultSet rs) throws SQLException, ReflectiveOperationException {
        String id = rs.getString("id");
        String employeeId = rs.getString("employee_id");
        LocalDate payPeriodStart = parseDate(rs.getString("pay_period_start"));
        LocalDate payPeriodEnd = parseDate(rs.getString("pay_period_end"));
        double baseSalary = rs.getDouble("base_salary");
        double overtimePay = rs.getDouble("overtime_pay");
        double bonus = rs.getDouble("bonus");
        double taxDeductions = rs.getDouble("tax_deductions");
        double otherDeductions = rs.getDouble("other_deductions");
        double netSalary = rs.getDouble("net_salary"); // Net salary is stored
        String statusStr = rs.getString("status");

        Payroll.PayrollStatus status = Payroll.PayrollStatus.PENDING; // Default
        try {
            if (statusStr != null && !statusStr.isEmpty()) {
                status = Payroll.PayrollStatus.valueOf(statusStr.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            // Log or handle invalid status from DB if necessary
        }

        Payroll payroll = new Payroll(); // Use default constructor
        payroll.setId(id); // Set the ID retrieved from DB
        payroll.setEmployeeId(employeeId);
        payroll.setPayPeriodStart(payPeriodStart);
        payroll.setPayPeriodEnd(payPeriodEnd);
        payroll.setBaseSalary(baseSalary); // Use setter, might trigger recalculation if implemented that way
        payroll.setOvertimePay(overtimePay);
        payroll.setBonus(bonus);
        payroll.setTaxDeductions(taxDeductions);
        payroll.setOtherDeductions(otherDeductions);
        payroll.setStatus(status);

        // Set netSalary directly using reflection, trusting the stored value
        try {
            Field netSalaryField = Payroll.class.getDeclaredField("netSalary");
            netSalaryField.setAccessible(true);
            netSalaryField.setDouble(payroll, netSalary);
            netSalaryField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // If reflection fails, rethrow or recalculate as a fallback
            payroll.calculateNetSalary(); // Recalculate if reflection fails
            // Rethrow wrapped exception if strict error handling is needed
            // throw new ReflectiveOperationException("Error setting netSalary via reflection", e);
        }

        return payroll;
    }

    /**
     * Inserts a new performance evaluation record into the database.
     * Assumes the PerformanceEvaluation object has a non-null, valid UUID assigned.
     *
     * @param evaluation The PerformanceEvaluation object to insert.
     * @return true if insertion was successful, false otherwise.
     */
    public boolean insertEvaluation(PerformanceEvaluation evaluation) {
        if (connection == null || evaluation == null || evaluation.getId() == null || evaluation.getId().trim().isEmpty()) {
            return false;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_EVALUATION_SQL)) {
            pstmt.setString(1, evaluation.getId());
            pstmt.setString(2, evaluation.getEmployeeId());
            pstmt.setString(3, evaluation.getEvaluationDate() != null ? evaluation.getEvaluationDate().format(DATE_FORMATTER) : null);
            pstmt.setInt(4, evaluation.getPerformanceRating());
            pstmt.setString(5, evaluation.getStrengths());
            pstmt.setString(6, evaluation.getAreasForImprovement());
            pstmt.setString(7, evaluation.getComments());
            pstmt.setString(8, evaluation.getReviewedBy());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            // if (e.getErrorCode() == 19) { ... } // Constraint violation
            // Error handled by caller or application logic
            return false;
        }
    }

    /**
     * Retrieves all performance evaluation records from the database.
     *
     * @return A List of PerformanceEvaluation objects, or an empty list if none found or error occurs.
     */
    public List<PerformanceEvaluation> getAllEvaluations() {
        List<PerformanceEvaluation> evaluationList = new ArrayList<>();
        if (connection == null) {
            return evaluationList;
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_EVALUATIONS_SQL)) {
            while (rs.next()) {
                evaluationList.add(mapResultSetToEvaluation(rs));
            }
        } catch (SQLException e) {
            // Error handled by caller or application logic
        }
        return evaluationList;
    }

    /**
     * Retrieves a single performance evaluation record by its String ID (UUID).
     *
     * @param id The String UUID of the evaluation record.
     * @return The PerformanceEvaluation object if found, otherwise null.
     */
    public PerformanceEvaluation getEvaluationById(String id) {
        if (connection == null || id == null || id.trim().isEmpty()) {
            return null;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_EVALUATION_BY_ID_SQL)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEvaluation(rs);
                } else {
                    return null; // Not found
                }
            }
        } catch (SQLException e) {
            // Error handled by caller or application logic
            return null;
        }
    }

    /**
     * Retrieves all performance evaluation records for a specific employee.
     *
     * @param employeeId The String UUID of the employee.
     * @return A List of PerformanceEvaluation objects for the employee, or an empty list.
     */
    public List<PerformanceEvaluation> getEvaluationsByEmployeeId(String employeeId) {
        List<PerformanceEvaluation> evaluationList = new ArrayList<>();
        if (connection == null || employeeId == null || employeeId.trim().isEmpty()) {
            return evaluationList;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_EVALUATIONS_BY_EMPLOYEE_ID_SQL)) {
            pstmt.setString(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    evaluationList.add(mapResultSetToEvaluation(rs));
                }
            }
        } catch (SQLException e) {
            // Error handled by caller or application logic
        }
        return evaluationList;
    }

    /**
     * Updates an existing performance evaluation record in the database.
     *
     * @param evaluation The PerformanceEvaluation object containing updated data (must have correct ID).
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateEvaluation(PerformanceEvaluation evaluation) {
        if (connection == null || evaluation == null || evaluation.getId() == null || evaluation.getId().trim().isEmpty()) {
            return false;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(UPDATE_EVALUATION_SQL)) {
            pstmt.setString(1, evaluation.getEmployeeId());
            pstmt.setString(2, evaluation.getEvaluationDate() != null ? evaluation.getEvaluationDate().format(DATE_FORMATTER) : null);
            pstmt.setInt(3, evaluation.getPerformanceRating());
            pstmt.setString(4, evaluation.getStrengths());
            pstmt.setString(5, evaluation.getAreasForImprovement());
            pstmt.setString(6, evaluation.getComments());
            pstmt.setString(7, evaluation.getReviewedBy());
            pstmt.setString(8, evaluation.getId()); // WHERE clause

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            // Error handled by caller or application logic
            return false;
        }
    }

    /**
     * Deletes a performance evaluation record from the database using its String ID (UUID).
     *
     * @param id The String UUID of the evaluation record to delete.
     * @return true if deletion was successful, false otherwise.
     */
    public boolean deleteEvaluation(String id) {
        if (connection == null || id == null || id.trim().isEmpty()) {
            return false;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(DELETE_EVALUATION_SQL)) {
            pstmt.setString(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            // Error handled by caller or application logic
            return false;
        }
    }

    /**
     * Helper method to map a row from a ResultSet to a PerformanceEvaluation object.
     *
     * @param rs The ResultSet cursor, positioned at the row to map.
     * @return A PerformanceEvaluation object populated with data.
     * @throws SQLException If a database access error occurs.
     */
    private PerformanceEvaluation mapResultSetToEvaluation(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String employeeId = rs.getString("employee_id");
        LocalDate evaluationDate = parseDate(rs.getString("evaluation_date")); // Use existing helper
        int rating = rs.getInt("rating");
        String strengths = rs.getString("strengths");
        String areasForImprovement = rs.getString("areas_for_improvement");
        String comments = rs.getString("comments");
        String reviewedBy = rs.getString("reviewed_by");

        // Use the constructor that takes all arguments, including the ID from the DB
        return new PerformanceEvaluation(id, employeeId, evaluationDate, rating, strengths,
                areasForImprovement, comments, reviewedBy);
    }

    /**
     * Ensures the default super administrator account exists in the database.
     * Creates it with a default password if it doesn't exist.
     */
    private void ensureSuperAdminExists() {
        String superAdminUsername = "super";
        String superAdminPassword = "super123"; // Default plain text password
        String superAdminFullName = "Super Administrator";
        User.UserRole superAdminRole = User.UserRole.SUPER_ADMIN;

        User existingAdmin = getUserByUsername(superAdminUsername);
        if (existingAdmin == null) {
            // Use the insertUser method which handles hashing
            boolean created = insertUser(superAdminUsername, superAdminFullName, superAdminPassword, superAdminRole.name());
            if (!created) {
                // Critical failure, application might not work without admin
                // Consider throwing a specific runtime exception here
            }
        }
        // else: Default admin already exists, no action needed.
    }


    // --- Connection Management ---
    /**
     * Closes the database connection. Should be called when the application shuts down.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Error handled by caller or application logic
            }
        }
    }
}