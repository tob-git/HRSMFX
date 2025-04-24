package com.example.hrsm2.whitebox;

import com.example.hrsm2.controller.PayrollController;
import com.example.hrsm2.model.Employee;
import com.example.hrsm2.model.Payroll;
import com.example.hrsm2.service.EmployeeService;
import com.example.hrsm2.service.PayrollService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class PayrollControllerWhiteBoxTest {

    @Mock
    private EmployeeService employeeService;

    @Mock
    private PayrollService payrollService;

    /** System‑under‑test (SUT) with mocked dependencies injected. */
    @InjectMocks
    private PayrollController controller;

    private Employee john;
    private Payroll johnPayroll;

    @BeforeEach
    void setUp() {
        john = new Employee("John", "Doe", "john.doe@example.com", "0123456789",
                LocalDate.of(2020, 1, 15), "IT", "Developer", 120_000.00);

        johnPayroll = new Payroll( john.getId(), LocalDate.of(2025, 4, 1),
                LocalDate.of(2025, 4, 30), 10_000.00);
    }

    /* --------------------------------------------------------------------- */
    /* Read‑only operations                                                 */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("getAllEmployees ‑ returns list from service")
    void getAllEmployees_returnsList() {
        List<Employee> expected = Arrays.asList(john);
        when(employeeService.getAllEmployees()).thenReturn(expected);

        List<Employee> actual = controller.getAllEmployees();

        assertSame(expected, actual, "Controller should delegate directly to service");
        verify(employeeService).getAllEmployees();
        verifyNoMoreInteractions(employeeService, payrollService);
    }

    @Test
    @DisplayName("getEmployeeById ‑ happy path & null path tested")
    void getEmployeeById_paths() {
        when(employeeService.getEmployeeById("e123")).thenReturn(john);

        assertEquals(john, controller.getEmployeeById("e123"));
        assertNull(controller.getEmployeeById("missing")); // default Mockito returns null

        verify(employeeService).getEmployeeById("e123");
        verify(employeeService).getEmployeeById("missing");
    }

    @Test
    @DisplayName("getAllPayrolls ‑ delegates correctly")
    void getAllPayrolls_delegation() {
        List<Payroll> list = Collections.singletonList(johnPayroll);
        when(payrollService.getAllPayrolls()).thenReturn(list);

        assertSame(list, controller.getAllPayrolls());
        verify(payrollService).getAllPayrolls();
    }

    /* --------------------------------------------------------------------- */
    /* Write operations (generate / update / process / pay)                 */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("generatePayroll ‑ passes args & returns service result")
    void generatePayroll_delegates() {
        when(payrollService.generatePayroll(john.getId(), LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 30)))
                .thenReturn(johnPayroll);

        Payroll generated = controller.generatePayroll(john.getId(), LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 30));

        assertEquals(johnPayroll, generated);
        verify(payrollService).generatePayroll(john.getId(), LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 30));
    }

    @Test
    @DisplayName("generatePayrollsForAllEmployees ‑ void path verified via interaction")
    void generatePayrollsForAllEmployees_invokesService() {
        controller.generatePayrollsForAllEmployees(LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 30));

        verify(payrollService).generatePayrollsForAllEmployees(LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 30));
    }

    @Test
    @DisplayName("updatePayroll ‑ returns service boolean and propagates")
    void updatePayroll_paths() {
        when(payrollService.updatePayroll(johnPayroll)).thenReturn(true).thenReturn(false);

        assertTrue(controller.updatePayroll(johnPayroll));  // first call true
        assertFalse(controller.updatePayroll(johnPayroll)); // second call false

        verify(payrollService, times(2)).updatePayroll(johnPayroll);
    }

    @Test
    @DisplayName("processPayroll & markPayrollAsPaid ‑ status transition delegation")
    void processAndPayPayroll() {
        when(payrollService.processPayroll("p1")).thenReturn(true);
        when(payrollService.markPayrollAsPaid("p1")).thenReturn(true);

        assertTrue(controller.processPayroll("p1"));
        assertTrue(controller.markPayrollAsPaid("p1"));

        verify(payrollService).processPayroll("p1");
        verify(payrollService).markPayrollAsPaid("p1");
    }

    /* --------------------------------------------------------------------- */
    /* Pure logic methods (no mocks)                                        */
    /* --------------------------------------------------------------------- */

    @Nested
    @DisplayName("calculateMonthlySalary ‑ edge & nominal cases")
    class MonthlySalaryTests {
        @ParameterizedTest(name = "salary={0}")
        @CsvSource({
                "120000,10000",  // standard annual salary
                "0,0"            // unpaid intern!
        })
        void monthlySalary(double annual, double expectedMonthly) {
            Employee e = new Employee("A", "B", "a@b.com", "0", LocalDate.now(), "D", "E", annual);
            assertEquals(expectedMonthly, controller.calculateMonthlySalary(e), 1e-6);
        }

        @Test
        void nullEmployee_returnsZero() {
            assertEquals(0.0, controller.calculateMonthlySalary(null));
        }
    }

    @Nested
    @DisplayName("calculateNetSalary ‑ covers all branches incl. invalid dates")
    class NetSalaryTests {

        @ParameterizedTest(name = "days={6}, result")
        @CsvSource({
                // base,overtime,bonus,tax,other,start,end,expected
                "10000,500,200,300,0,2025-04-01,2025-04-30,\"(10000+500+200-300)*1\"",
                "3000,0,0,0,0,2025-04-10,2025-04-15,\"(3000)*6/30\""
        })
        void validPeriods(String baseStr, String otStr, String bonusStr, String taxStr, String otherStr,
                          LocalDate start, LocalDate end, String expr) {
            double base = Double.parseDouble(baseStr);
            double ot = Double.parseDouble(otStr);
            double bonus = Double.parseDouble(bonusStr);
            double tax = Double.parseDouble(taxStr);
            double other = Double.parseDouble(otherStr);
            long days = java.time.temporal.ChronoUnit.DAYS.between(start, end.plusDays(1));
            double expected = (base + ot + bonus - tax - other) * (days / 30.0);

            double actual = controller.calculateNetSalary(base, ot, bonus, tax, other, start, end);
            assertEquals(expected, actual, 1e-6);
        }

        @Test
        void endDateBeforeStart_returnsZero() {
            double result = controller.calculateNetSalary(1000, 0, 0, 0, 0,
                    LocalDate.of(2025, 4, 10), LocalDate.of(2025, 4, 5));
            assertEquals(0.0, result);
        }

        @Test
        void nullDates_returnsZero() {
            assertEquals(0.0, controller.calculateNetSalary(1,1,1,1,1,null,null));
        }
        @Test
        void nullDates_returnsZero1() {
            assertEquals(0.0, controller.calculateNetSalary(1,1,1,1,1,LocalDate.of(2025, 4, 10),null));
        }
    }

    /* --------------------------------------------------------------------- */
    /* Constructor behaviour                                                */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("default constructor uses singleton instances (smoke test)")
    void defaultConstructor_smoke() {
        PayrollController defaultCtl = new PayrollController();
        // We cannot assert singleton internals here without reflection; just ensure calls work
        assertDoesNotThrow(defaultCtl::shutdown);
    }
}

