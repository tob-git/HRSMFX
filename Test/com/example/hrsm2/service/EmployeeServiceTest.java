package com.example.hrsm2.service;

import com.example.hrsm2.model.Employee;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmployeeServiceTest {

private static EmployeeService operation;
private static String testEmployeeId;

@BeforeAll
static void initAll() {
    operation = EmployeeService.getInstance();
}

@Test
@Order(1)
@DisplayName("1. Singleton Instance Test")
void getInstance() {
    assertNotNull(operation, "EmployeeService instance should not be null");
}

@ParameterizedTest
@Order(2)
@DisplayName("2. Add Employee Test")
@CsvSource({
        "ID1, Alice, Johnson, alice.johnson@example.com, 1234567890, HR, 5000.00",
        "ID2, Bob, Smith, bob.smith@example.com, 2345678901, IT, 6500.50",
        "ID3, Clara, Davis, clara.davis@example.com, 3456789012, Finance, 7200.75",
        "ID4, David, Wilson, david.wilson@example.com, 4567890123, Marketing, 4800.25",
        "ID5, Eva, Brown, eva.brown@example.com, 5678901234, Sales, 5300.00"
})
void addEmployee(String ID, String FirstName, String LastName, String Email, String Phone, String Department, double Salary) {
    Employee employee = new Employee(ID,FirstName,LastName, Email, Phone,
            LocalDate.of(2023, 11, 15), Department, Department, Salary);
//        employee.setId(ID);
//        employee.setFirstName(FirstName);
//        employee.setLastName(LastName);
//        employee.setEmail(Email);
//        employee.setPhone(Phone);
//        employee.setDepartment(Department);
//        employee.setSalary(Salary);

    //boolean added = operation.addEmployee(employee);
    //testEmployeeId = employee.getId(); // Save ID for future tests

    assertTrue(operation.addEmployee(employee), "Employee should be added successfully");
    assertEquals(employee.getId(),ID, "Employee ID should match");
}

@Test
@Order(3)
@DisplayName("3. Get All Employees Test")
void getAllEmployees() {
    List<Employee> employees = operation.getAllEmployees();
    assertNotNull(employees, "List of employees should not be null");
    assertFalse(employees.isEmpty(), "There should be at least one employee");
}

@ParameterizedTest
@Order(4)
@DisplayName("4. Get Employee by ID Test")
@CsvSource({
        "ID1, Alice, Johnson, alice.johnson@example.com, 1234567890, HR, 5000.00",
        "ID2, Bob, Smith, bob.smith@example.com, 2345678901, IT, 6500.50",
        "ID3, Clara, Davis, clara.davis@example.com, 3456789012, Finance, 7200.75",
        "ID4, David, Wilson, david.wilson@example.com, 4567890123, Marketing, 4800.25",
        "ID5, Eva, Brown, eva.brown@example.com, 5678901234, Sales, 5300.00"
})
void getEmployeeById(String ID, String FirstName, String LastName, String Email, String Phone, String Department, double Salary) {
    Employee fetched = operation.getEmployeeById(ID);
    assertNotNull(fetched, "Employee should be found by ID");
    assertEquals(FirstName+LastName+Email+Phone+Department+Salary, fetched.getFirstName()+fetched.getLastName()+fetched.getEmail()+fetched.getPhone()+fetched.getDepartment()+fetched.getSalary(), "Employee name should match");   //////// recheck + sign
}

       /*
@ParameterizedTest
@Order(5)
@DisplayName("5. Update Employee Test")
@CsvSource({
        "ID1",
        "ID2",
        "ID3",
        "ID4",
        "ID5"
})
void updateEmployee(String ID) {
    Employee employee = operation.getEmployeeById(ID);
    employee.setSalary(8000.50);
    employee.setDepartment("7amada");

    boolean updated = operation.updateEmployee(employee);
    assertTrue(updated, "Employee should be updated successfully");

    Employee updatedEmp = operation.getEmployeeById(testEmployeeId);
    assertEquals(8000.50, updatedEmp.getSalary(), "Updated salary should match");
    assertEquals("7amada", updatedEmp.getDepartment(), "Updated department should match");
}   //*/
@ParameterizedTest
@Order(5)
@DisplayName("5. Fail to Update an invalid Employee Test")
@CsvSource({
       "ID-1_NOT_FOUND",
       "ID-2_NOT_FOUND",
       "ID-3_NOT_FOUND",
       "ID-4_NOT_FOUND",
       "ID-5_NOT_FOUND"
})
void updateEmployee(String ID) {
   Employee employee = operation.getEmployeeById(ID);
   assertNull(employee, "Employee should not be found");
   assertFalse(operation.updateEmployee(employee), "Employee should not be updated");
}

//        /*
@ParameterizedTest
@Order(6)
@DisplayName("6. Search Employees Test")
@CsvSource({
        "ID1, Alice, Johnson, alice.johnson@example.com, 1234567890, HR, 5000.00",
        "ID2, Bob, Smith, bob.smith@example.com, 2345678901, IT, 6500.50",
        "ID3, Clara, Davis, clara.davis@example.com, 3456789012, Finance, 7200.75",
        "ID4, David, Wilson, david.wilson@example.com, 4567890123, Marketing, 4800.25",
        "ID5, Eva, Brown, eva.brown@example.com, 5678901234, Sales, 5300.00"
})
void searchEmployees(String ID, String FirstName, String LastName, String Email, String Phone, String Department, double Salary) {
    Employee employee = new Employee(ID,FirstName,LastName, Email, Phone,
            LocalDate.of(2023, 11, 15), Department, Department, Salary);

    List<Employee> results = operation.searchEmployees("Eva");
    //Employee employee_for_test = operation.getEmployeeById(ID);
    assertNotNull(results, "Search results should not be null");
    //assertTrue(results.contains(employee), "Search should return the test employee");
    assertTrue(results.stream().anyMatch(e -> e.getId().equals(ID)), "Search should return the test employee");
}
//*/

@ParameterizedTest
@Order(7)
@DisplayName("7. Delete Employee Test")
@CsvSource({
        "ID1",
        "ID2",
        "ID3",
        "ID4",
        "ID5"
})
void deleteEmployee(String ID) {
    boolean deleted = operation.deleteEmployee(ID);
    assertTrue(deleted, "Employee should be deleted successfully");

    Employee shouldBeNull = operation.getEmployeeById(ID);
    assertNull(shouldBeNull, "Deleted employee should not be found");
}

@Test
@Order(8)
@DisplayName("8. Close DB Connection Test")
void closeDatabaseConnection() {
    assertDoesNotThrow(() -> operation.closeDatabaseConnection(), "Closing DB connection should not throw an exception");
}
}
