package com.example.hrsm2.unittest;

import com.example.hrsm2.model.*;
import com.example.hrsm2.service.PayrollService;
import com.example.hrsm2.service.EmployeeService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class PayrollServiceTest {

    private PayrollService operation;
    private EmployeeService employeeService;
    private Employee employee;
    private Payroll payroll;
    @BeforeEach
    void setUp() {
        operation = PayrollService.getInstance();
        employeeService= EmployeeService.getInstance();

    }

    @Test
    @Order(1)
    @DisplayName("1. Get PayrollService instance")
    void testGetInstance() {
        PayrollService anotherInstance = PayrollService.getInstance();
        assertNotNull(operation);
        assertSame(operation, anotherInstance);
    }

    @Test
    @Order(2)
    @DisplayName("2. Get all payrolls (should not be null)")
    void testGetAllPayrolls() {
        List<Payroll> payrolls = operation.getAllPayrolls();
        assertNotNull(payrolls);
    }

    @ParameterizedTest
    @Order(3)
    @DisplayName("3. Generate payroll for an employee")
    @CsvSource({
                "EMP001, 2024-01-01, 2024-01-31",
                "EMP002, 2024-02-01, 2024-02-28",
                "EMP003, 2024-03-01, 2024-03-31",
                "EMP004, 2024-04-01, 2024-04-30"
    })
    void testGeneratePayroll(String employeeId, LocalDate payPeriodStart, LocalDate payPeriodEnd) {
        employee= new Employee(employeeId,"FirstName","LastName", "Email", "Phone",
                LocalDate.of(2023, 11, 15), "Department", "Department", 5000.00);
        employeeService.addEmployee(employee);

        payroll = operation.generatePayroll(employeeId, payPeriodStart, payPeriodEnd);
        assertNotNull(payroll);
        assertEquals(employeeId, payroll.getEmployeeId());
        assertEquals(payPeriodStart, payroll.getPayPeriodStart());
        assertEquals(payPeriodEnd, payroll.getPayPeriodEnd());

        employeeService.deleteEmployee(employeeId);
        operation.deletePayroll(payroll.getId());
    }

    @ParameterizedTest
    @Order(4)
    @DisplayName("4. Get payroll by ID")
    @CsvSource({
            "EMP001",
            "EMP002",
            "EMP003",
            "EMP004"
    })
    void testGetPayrollById(String employeeID) {
        employee= new Employee(employeeID,"FirstName","LastName", "Email", "Phone",
                LocalDate.of(2023, 11, 15), "Department", "Department", 5000.00);
        employeeService.addEmployee(employee);
        payroll = operation.generatePayroll(employeeID, LocalDate.now(), LocalDate.now().plusDays(30));
        Payroll found = operation.getPayrollById(payroll.getId());

        assertNotNull(found);
        assertEquals(payroll.getId(), found.getId());

        employeeService.deleteEmployee(employeeID);
        operation.deletePayroll(payroll.getId());
    }

    @ParameterizedTest
    @Order(5)
    @DisplayName("5. Update payroll")
    @CsvSource({
            "EMP001",
            "EMP002",
            "EMP003",
            "EMP004"
    })
    void testUpdatePayroll(String employeeID) {
        employee= new Employee(employeeID,"FirstName","LastName", "Email", "Phone",
                LocalDate.of(2023, 11, 15), "Department", "Department", 5000.00);
        employeeService.addEmployee(employee);
        payroll = operation.generatePayroll(employeeID, LocalDate.now(), LocalDate.now().plusDays(30));
        payroll.setTaxDeductions(100);
        payroll.setOtherDeductions(50);

        assertTrue(operation.updatePayroll(payroll));

        employeeService.deleteEmployee(employeeID);
        operation.deletePayroll(payroll.getId());
    }

    @ParameterizedTest
    @Order(6)
    @DisplayName("6. Process payroll")
    @CsvSource({
            "EMP001",
            "EMP002",
            "EMP003",
            "EMP004"
    })
    void testProcessPayroll(String employeeID) {
        employee= new Employee(employeeID,"FirstName","LastName", "Email", "Phone",
                LocalDate.of(2023, 11, 15), "Department", "Department", 5000.00);
        employeeService.addEmployee(employee);
        payroll = operation.generatePayroll(employeeID, LocalDate.now(), LocalDate.now().plusDays(30));

        assertTrue(operation.processPayroll(payroll.getId()));

        employeeService.deleteEmployee(employeeID);
        operation.deletePayroll(payroll.getId());
    }

    @ParameterizedTest
    @Order(7)
    @DisplayName("7. Mark payroll as paid")
    @CsvSource({
            "EMP001",
            "EMP002",
            "EMP003",
            "EMP004"
    })
    void testMarkPayrollAsPaid(String employeeID) {
        employee= new Employee(employeeID,"FirstName","LastName", "Email", "Phone",
                LocalDate.of(2023, 11, 15), "Department", "Department", 5000.00);
        employeeService.addEmployee(employee);
        payroll = operation.generatePayroll(employeeID, LocalDate.now(), LocalDate.now().plusDays(30));
        operation.processPayroll(payroll.getId());

        assertTrue(operation.markPayrollAsPaid(payroll.getId()));

        employeeService.deleteEmployee(employeeID);
        operation.deletePayroll(payroll.getId());
    }

    @ParameterizedTest
    @Order(8)
    @DisplayName("8. Delete payroll")
    @CsvSource({
            "EMP001",
            "EMP002",
            "EMP003",
            "EMP004"
    })
    void testDeletePayroll(String employeeID) {
        employee= new Employee(employeeID,"FirstName","LastName", "Email", "Phone",
                LocalDate.of(2023, 11, 15), "Department", "Department", 5000.00);
        employeeService.addEmployee(employee);
        payroll = operation.generatePayroll(employeeID, LocalDate.now(), LocalDate.now().plusDays(30));

        assertTrue(operation.deletePayroll(payroll.getId()));

        employeeService.deleteEmployee(employeeID);
    }

    @Test
    @Order(9)
    @DisplayName("9. Generate payrolls for all employees")
    void testGeneratePayrollsForAllEmployees() {

        assertNotNull(operation.getAllPayrolls());
    }

    @ParameterizedTest
    @Order(10)
    @DisplayName("10. Get payrolls by employee ID")
    @CsvSource({
            "EMP001",
            "EMP002",
            "EMP003",
            "EMP004"
    })
    void testGetPayrollsByEmployeeId(String employeeID) {
        employee= new Employee(employeeID,"FirstName","LastName", "Email", "Phone",
                LocalDate.of(2023, 11, 15), "Department", "Department", 5000.00);
        employeeService.addEmployee(employee);
        payroll= operation.generatePayroll(employeeID, LocalDate.now(), LocalDate.now().plusDays(30));
        List<Payroll> payrolls = operation.getPayrollsByEmployeeId(employeeID);

        assertNotNull(payrolls);

        employeeService.deleteEmployee(employeeID);
        operation.deletePayroll(payroll.getId());
    }
}
