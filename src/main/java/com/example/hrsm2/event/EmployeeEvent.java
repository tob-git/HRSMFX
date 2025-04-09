package com.example.hrsm2.event;

import com.example.hrsm2.model.Employee;
import javafx.event.Event;
import javafx.event.EventType;

/**
 * Custom event class for employee-related events.
 * This allows controllers to listen for employee updates.
 */
public class EmployeeEvent extends Event {
    
    public static final EventType<EmployeeEvent> EMPLOYEE_ADDED = new EventType<>(Event.ANY, "EMPLOYEE_ADDED");
    public static final EventType<EmployeeEvent> EMPLOYEE_UPDATED = new EventType<>(Event.ANY, "EMPLOYEE_UPDATED");
    public static final EventType<EmployeeEvent> EMPLOYEE_DELETED = new EventType<>(Event.ANY, "EMPLOYEE_DELETED");
    
    private final Employee employee;
    
    public EmployeeEvent(EventType<? extends Event> eventType, Employee employee) {
        super(eventType);
        this.employee = employee;
    }
    
    public Employee getEmployee() {
        return employee;
    }
} 