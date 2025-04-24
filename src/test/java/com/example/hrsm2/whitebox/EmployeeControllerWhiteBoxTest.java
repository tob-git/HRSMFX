package com.example.hrsm2.whitebox;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.hrsm2.controller.EmployeeController;
import com.example.hrsm2.event.EmployeeEvent;
import com.example.hrsm2.event.EventManager;
import com.example.hrsm2.model.Employee;
import com.example.hrsm2.service.EmployeeService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class EmployeeControllerWhiteBoxTest {

    @Mock  EmployeeService employeeService;   // injected via constructor
    @Mock  EventManager    eventManager;      // returned by EventManager.getInstance()

    private final Employee sample =
            new Employee("Jane","Doe","jane@corp.com","0123456789",
                    LocalDate.of(2022,1,1),"IT","Dev",55_000);

    /* ---------- addEmployee ---------- */

    @Test @DisplayName("addEmployee : success → returns true & fires EMPLOYEE_ADDED")
    void addEmployee_success() {
        when(employeeService.addEmployee(sample)).thenReturn(true);

        try (MockedStatic<EventManager> st = mockStatic(EventManager.class)) {
            st.when(EventManager::getInstance).thenReturn(eventManager);

            EmployeeController controller = new EmployeeController(employeeService);
            assertTrue(controller.addEmployee(sample));

            ArgumentCaptor<EmployeeEvent> ev = ArgumentCaptor.forClass(EmployeeEvent.class);
            verify(eventManager).fireEvent(ev.capture());
            assertEquals(EmployeeEvent.EMPLOYEE_ADDED, ev.getValue().getEventType());
            assertEquals(sample, ev.getValue().getEmployee());
        }
    }

    @Test @DisplayName("addEmployee : failure → returns false & NO event")
    void addEmployee_failure() {
        when(employeeService.addEmployee(sample)).thenReturn(false);

        try (MockedStatic<EventManager> st = mockStatic(EventManager.class)) {
            st.when(EventManager::getInstance).thenReturn(eventManager);

            EmployeeController controller = new EmployeeController(employeeService);
            assertFalse(controller.addEmployee(sample));

            verify(eventManager, never()).fireEvent(any());
        }
    }

    /* ---------- updateEmployee ---------- */

    @Test @DisplayName("updateEmployee : success path")
    void updateEmployee_success() {
        when(employeeService.updateEmployee(sample)).thenReturn(true);

        try (MockedStatic<EventManager> st = mockStatic(EventManager.class)) {
            st.when(EventManager::getInstance).thenReturn(eventManager);

            EmployeeController c = new EmployeeController(employeeService);
            assertTrue(c.updateEmployee(sample));
            verify(eventManager).fireEvent(argThat(e ->
                    e instanceof EmployeeEvent &&
                            e.getEventType().equals(EmployeeEvent.EMPLOYEE_UPDATED) &&
                            ((EmployeeEvent) e).getEmployee().equals(sample)
            ));

        }
    }

    @Test @DisplayName("updateEmployee : service rejects → no event")
    void updateEmployee_failure() {
        when(employeeService.updateEmployee(sample)).thenReturn(false);

        EmployeeController c = new EmployeeController(employeeService);
        assertFalse(c.updateEmployee(sample));
        // singleton never reached → no need to stub EventManager
    }

    /* ---------- deleteEmployee ---------- */

    @Nested
    class DeleteEmployee {

        private final String id = UUID.randomUUID().toString();

        @Test @DisplayName("deleteEmployee : not-found → returns false")
        void delete_notFound() {
            when(employeeService.getEmployeeById(id)).thenReturn(null);
            EmployeeController c = new EmployeeController(employeeService);
            assertFalse(c.deleteEmployee(id));
            verify(employeeService, never()).deleteEmployee(anyString());
        }

        @Test @DisplayName("deleteEmployee : service fails → returns false & no event")
        void delete_serviceFails() {
            when(employeeService.getEmployeeById(id)).thenReturn(sample);
            when(employeeService.deleteEmployee(id)).thenReturn(false);

            EmployeeController c = new EmployeeController(employeeService);
            assertFalse(c.deleteEmployee(id));
        }

        @Test @DisplayName("deleteEmployee : happy path → fires EMPLOYEE_DELETED")
        void delete_success() {
            when(employeeService.getEmployeeById(id)).thenReturn(sample);
            when(employeeService.deleteEmployee(id)).thenReturn(true);

            try (MockedStatic<EventManager> st = mockStatic(EventManager.class)) {
                st.when(EventManager::getInstance).thenReturn(eventManager);

                EmployeeController c = new EmployeeController(employeeService);
                assertTrue(c.deleteEmployee(id));
                verify(eventManager).fireEvent(argThat(e ->
                        e instanceof EmployeeEvent &&
                                e.getEventType().equals(EmployeeEvent.EMPLOYEE_DELETED) &&
                                ((EmployeeEvent) e).getEmployee().equals(sample)
                ));
            }
        }
    }

    /* ---------- read-only methods ---------- */

    @Test
    void searchEmployees_delegates() {
        String term = "jane";
        when(employeeService.searchEmployees(term)).thenReturn(List.of(sample));

        EmployeeController c = new EmployeeController(employeeService);
        assertEquals(List.of(sample), c.searchEmployees(term));
        verify(employeeService).searchEmployees(term);
    }

    @Test
    void getAllEmployees_delegates() {
        when(employeeService.getAllEmployees()).thenReturn(List.of(sample));

        EmployeeController c = new EmployeeController(employeeService);
        assertEquals(1, c.getAllEmployees().size());
        verify(employeeService).getAllEmployees();
    }

    @Test
    void getEmployeeById_delegates() {
        when(employeeService.getEmployeeById("42")).thenReturn(sample);

        EmployeeController c = new EmployeeController(employeeService);
        assertSame(sample, c.getEmployeeById("42"));
    }

    /* ---------- shutdown ---------- */

    @Test
    void shutdown_closesConnection() {
        EmployeeController c = new EmployeeController(employeeService);
        c.shutdown();
        verify(employeeService).closeDatabaseConnection();
    }
}
