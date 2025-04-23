package com.example.hrsm2.unittest;

import com.example.hrsm2.model.*;
import com.example.hrsm2.service.LeaveRequestService;
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
class LeaveRequestServiceTest {

    private LeaveRequestService operation;
    private LeaveRequest Request;

    @BeforeEach
    void setup() {
        operation = LeaveRequestService.getInstance();
    }


    @Test
    @Order(1)
    @DisplayName("1. Get singleton instance")
    void getInstance() {
        LeaveRequestService test_instance = LeaveRequestService.getInstance();
        assertSame(operation,test_instance);

    }

    @ParameterizedTest
    @Order(2)
    @DisplayName("2. Submit valid leave request")
    @CsvSource({
            "EMP001",
            "EMP002",
            "EMP003",
            "EMP004"
    })
    void submitLeaveRequest(String employeeId) {
        Request = new LeaveRequest(employeeId, LocalDate.now().plusDays(200),
                LocalDate.now().plusDays(201), "Vacation");

        assertTrue(operation.submitLeaveRequest(Request));
        assertNotNull(Request);

        operation.deleteLeaveRequest(Request.getId());
    }

    @Test
    @Order(3)
    @DisplayName("3. Get all leave requests")
    void getAllLeaveRequests() {
        List<LeaveRequest> requests = operation.getAllLeaveRequests();
        assertFalse(requests.isEmpty());
        assertNotNull(requests);
    }

    @ParameterizedTest
    @Order(4)
    @DisplayName("4. Get leave request by ID")
    @CsvSource({
            "EMP001",
            "EMP002",
            "EMP003",
            "EMP004"
    })
    void getLeaveRequestById(String employeeId) {
        Request = new LeaveRequest(employeeId, LocalDate.now().minusWeeks(5),
                LocalDate.now().minusWeeks(4), "Vacation");

        operation.submitLeaveRequest(Request);
        LeaveRequest result = operation.getLeaveRequestById(Request.getId());

        assertNotNull(result);
        assertEquals(Request.getId(), result.getId());

        operation.deleteLeaveRequest(Request.getId());
    }

    @ParameterizedTest
    @Order(5)
    @DisplayName("5. Get leave requests for specific employee")
    @CsvSource({
            "EMP001",
            "EMP002",
            "EMP003",
            "EMP004"
    })
    void getLeaveRequestsForEmployee(String employeeId) {
        Request = new LeaveRequest(employeeId, LocalDate.now().plusDays(5), LocalDate.now().plusDays(6), "Vacation");
        operation.submitLeaveRequest(Request);

        LeaveRequest New_Request = new LeaveRequest(employeeId, LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(19), "Doctor");

        operation.submitLeaveRequest(New_Request);
        List<LeaveRequest> requests = operation.getLeaveRequestsForEmployee(employeeId);

        assertFalse(requests.isEmpty());
        assertNotNull(requests);

        operation.deleteLeaveRequest(Request.getId());
        operation.deleteLeaveRequest(New_Request.getId());
    }

    @ParameterizedTest
    @Order(6)
    @DisplayName("6. Fail to Submit two leave requests with conflicting dates")
    @CsvSource({
            "EMP001",
            "EMP002",
            "EMP003",
            "EMP004"
    })
    void testSubmitLeaveRequestsWithConflictingDates(String employeeId) {
        Request = new LeaveRequest(employeeId, LocalDate.now().plusDays(20),
                LocalDate.now().plusDays(24), "Vacation");
        operation.submitLeaveRequest(Request);
        LeaveRequest New_Request = new LeaveRequest(employeeId, LocalDate.now().plusDays(20),
                LocalDate.now().plusDays(24), "Doctor");
        operation.submitLeaveRequest(New_Request);

        assertFalse(operation.submitLeaveRequest(New_Request));
        assertNull(New_Request.getId());

        operation.deleteLeaveRequest(Request.getId());
    }

    @ParameterizedTest
    @Order(7)
    @DisplayName("7. Approve pending leave request")
    @CsvSource({
            "EMP001",
            "EMP002",
            "EMP003",
            "EMP004"
    })
    void approveLeaveRequest(String employeeId) {
        Request = new LeaveRequest(employeeId, LocalDate.now().plusWeeks(8),
                LocalDate.now().plusWeeks(9), "Vacation");
        operation.submitLeaveRequest(Request);
        boolean success = operation.approveLeaveRequest(Request.getId(), "Enjoy your time");
        LeaveRequest updated = operation.getLeaveRequestById(Request.getId());

        assertTrue(success);
        assertEquals(LeaveRequest.LeaveStatus.APPROVED, updated.getStatus());

        operation.deleteLeaveRequest(Request.getId());
    }

    @ParameterizedTest
    @Order(8)
    @DisplayName("8. Get approved leave days for employee")
    @CsvSource({
            "EMP001",
            "EMP002",
            "EMP003",
            "EMP004"
    })
    void getApprovedLeaveDaysForEmployee(String employeeId) {
        Request = new LeaveRequest(employeeId, LocalDate.now().plusWeeks(8),
                LocalDate.now().plusWeeks(9), "Vacation");
        LeaveRequest New_Request = new LeaveRequest(employeeId, LocalDate.now().plusDays(20),
                LocalDate.now().plusDays(24), "Doctor");

        operation.submitLeaveRequest(Request);
        operation.submitLeaveRequest(New_Request);

        operation.approveLeaveRequest(Request.getId(), "Enjoy your time");
        operation.approveLeaveRequest(New_Request.getId(), "Get well soon");
        int days = operation.getApprovedLeaveDaysForEmployee(employeeId);

        assertTrue(days >= 2);

        operation.deleteLeaveRequest(Request.getId());
        operation.deleteLeaveRequest(New_Request.getId());
    }

    @ParameterizedTest
    @Order(9)
    @DisplayName("9. Update leave request comments")
    @CsvSource({
            "EMP001",
            "EMP002",
            "EMP003",
            "EMP004"
    })
    void updateLeaveRequest(String employeeId) {
        Request = new LeaveRequest(employeeId, LocalDate.now().minusDays(4),
                LocalDate.now().minusDays(1), "Vacation");
        operation.submitLeaveRequest(Request);
        Request.setManagerComments("Updated comment");

        assertTrue(operation.updateLeaveRequest(Request));
        assertEquals("Updated comment", operation.getLeaveRequestById(Request.getId()).getManagerComments());

        operation.deleteLeaveRequest(Request.getId());
    }

    @ParameterizedTest
    @Order(10)
    @DisplayName("10. Reject leave request with comment.")
    @CsvSource({
            "EMP001",
            "EMP002",
            "EMP003",
            "EMP004"
    })
    void rejectLeaveRequest(String employeeId) {
        Request = new LeaveRequest(employeeId, LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(6), "Vacation");
        operation.submitLeaveRequest(Request);
        boolean success = operation.rejectLeaveRequest(Request.getId(), "Enough Vacations");

        assertTrue(success);

        operation.deleteLeaveRequest(Request.getId());
    }


    @ParameterizedTest
    @Order(11)
    @DisplayName("11. Can't reject an approved (Not Pending) leave request ")
    @CsvSource({
            "EMP001",
            "EMP002",
            "EMP003",
            "EMP004"
    })
    void RejectAnApprovedLeaveRequest(String employeeId) {
        Request = new LeaveRequest(employeeId, LocalDate.now().plusWeeks(20),
                LocalDate.now().plusWeeks(21), "Vacation");
        operation.submitLeaveRequest(Request);
        boolean success = operation.approveLeaveRequest(Request.getId(), "Enjoy your time");
        LeaveRequest updated = operation.getLeaveRequestById(Request.getId());

        assertTrue(success);  // the request is accepted
        assertEquals(LeaveRequest.LeaveStatus.APPROVED, updated.getStatus());

        boolean Fails = operation.rejectLeaveRequest(Request.getId(), "Enough Vacations");

        assertFalse(Fails); // cant reject an accepted leave request

        operation.deleteLeaveRequest(Request.getId());
    }


    @Test
    @Order(12)
    @DisplayName("12. Delete leave request")
    void deleteLeaveRequest() {
        Request = new LeaveRequest("E010", LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(4), "Vacation");
        operation.submitLeaveRequest(Request);
        boolean success = operation.deleteLeaveRequest(Request.getId());
        assertTrue(success);
        LeaveRequest deleted = operation.getLeaveRequestById(Request.getId());
        assertNull(deleted);
    }

}
