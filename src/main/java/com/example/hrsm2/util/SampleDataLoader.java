package com.example.hrsm2.util;

import com.example.hrsm2.model.Employee;
import com.example.hrsm2.model.LeaveRequest;
import com.example.hrsm2.model.PerformanceEvaluation;
import com.example.hrsm2.model.User;
import com.example.hrsm2.service.EmployeeService;
import com.example.hrsm2.service.LeaveRequestService;
import com.example.hrsm2.service.PerformanceEvaluationService;
import com.example.hrsm2.service.PayrollService;
import com.example.hrsm2.service.UserService;

import java.time.LocalDate;

public class SampleDataLoader {

    private static final EmployeeService employeeService = EmployeeService.getInstance();
    private static final LeaveRequestService leaveRequestService = LeaveRequestService.getInstance();
    private static final PerformanceEvaluationService evaluationService = PerformanceEvaluationService.getInstance();
    private static final PayrollService payrollService = PayrollService.getInstance();
    private static final UserService userService = UserService.getInstance();

    public static void loadSampleData() {
        // Load sample users using the new SQL statements via UserService
        loadSampleUsers();

        // Check if employee data already exists (using the service that calls our new CRUD methods)
        if (!employeeService.getAllEmployees().isEmpty()) {
            return;
        }

        // --- Create Sample Employees ---
        Employee john = new Employee(
                "John", "Doe", "john.doe@example.com", "555-123-4567",
                LocalDate.of(2020, 1, 15), "Engineering", "Software Engineer", 85000.0);
        Employee jane = new Employee(
                "Jane", "Smith", "jane.smith@example.com", "555-987-6543",
                LocalDate.of(2019, 3, 10), "Marketing", "Marketing Manager", 92000.0);
        Employee bob = new Employee(
                "Bob", "Johnson", "bob.johnson@example.com", "555-456-7890",
                LocalDate.of(2021, 5, 20), "Finance", "Financial Analyst", 78000.0);
        Employee sarah = new Employee(
                "Sarah", "Williams", "sarah.williams@example.com", "555-789-0123",
                LocalDate.of(2018, 11, 5), "Human Resources", "HR Specialist", 75000.0);
        Employee michael = new Employee(
                "Michael", "Brown", "michael.brown@example.com", "555-234-5678",
                LocalDate.of(2022, 2, 8), "Engineering", "DevOps Engineer", 88000.0);

        // Insert employees using EmployeeService (which internally calls DatabaseDriver's new SQL CRUD methods)
        employeeService.addEmployee(john);
        employeeService.addEmployee(jane);
        employeeService.addEmployee(bob);
        employeeService.addEmployee(sarah);
        employeeService.addEmployee(michael);

        // --- Create Sample Leave Requests ---
        LeaveRequest johnLeave = new LeaveRequest(
                john.getId(),
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(15),
                "Family vacation"
        );
        LeaveRequest janeLeave = new LeaveRequest(
                jane.getId(),
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(6),
                "Doctor's appointment"
        );
        LeaveRequest approvedLeave = new LeaveRequest(
                bob.getId(),
                LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(5),
                "Personal leave"
        );
        approvedLeave.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        approvedLeave.setManagerComments("Approved as requested");

        LeaveRequest rejectedLeave = new LeaveRequest(
                sarah.getId(),
                LocalDate.now().minusDays(3),
                LocalDate.now().plusDays(3),
                "Training course"
        );
        rejectedLeave.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        rejectedLeave.setManagerComments("Critical project deadline, please reschedule");

        // Use LeaveRequestService to submit and update leave requests
        leaveRequestService.submitLeaveRequest(johnLeave);
        leaveRequestService.submitLeaveRequest(janeLeave);
        leaveRequestService.updateLeaveRequest(approvedLeave);
        leaveRequestService.updateLeaveRequest(rejectedLeave);

        // --- Create Sample Performance Evaluations ---
        PerformanceEvaluation johnEval = new PerformanceEvaluation(
                john.getId(),
                4,
                "Strong technical skills, good problem solver",
                "Could improve communication with non-technical team members",
                "Overall a valuable team member",
                "Alex Manager"
        );
        johnEval.setEvaluationDate(LocalDate.now().minusMonths(2));

        PerformanceEvaluation janeEval = new PerformanceEvaluation(
                jane.getId(),
                5,
                "Excellent leadership, achieved all targets",
                "Could delegate more tasks",
                "Exceptional performance this year",
                "Chris Director"
        );
        janeEval.setEvaluationDate(LocalDate.now().minusMonths(1));

        PerformanceEvaluation bobEval = new PerformanceEvaluation(
                bob.getId(),
                3,
                "Good analytical skills, accurate reporting",
                "Needs to meet deadlines more consistently",
                "Satisfactory performance, with room for growth",
                "Diana Manager"
        );
        bobEval.setEvaluationDate(LocalDate.now().minusMonths(3));

        // Insert evaluations using PerformanceEvaluationService
        evaluationService.addEvaluation(johnEval);
        evaluationService.addEvaluation(janeEval);
        evaluationService.addEvaluation(bobEval);

        // --- Generate Sample Payrolls ---
        LocalDate startOfLastMonth = LocalDate.now().withDayOfMonth(1).minusMonths(1);
        LocalDate endOfLastMonth = startOfLastMonth.withDayOfMonth(startOfLastMonth.lengthOfMonth());
        payrollService.generatePayrollsForAllEmployees(startOfLastMonth, endOfLastMonth);
    }

    private static void loadSampleUsers() {
        // Check if an HR admin exists using UserService (which uses the new SQL statements)
        boolean hasHrUsers = userService.getAllUsers().stream()
                .anyMatch(user -> user.getRole() == User.UserRole.HR_ADMIN);
        if (!hasHrUsers) {
            // The DatabaseDriver will hash the password during user creation.
            userService.createUser("hr", "hr", "John Doe", User.UserRole.HR_ADMIN);
        }
    }
}
