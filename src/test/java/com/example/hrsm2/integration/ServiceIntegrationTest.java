package com.example.hrsm2.integration;

import com.example.hrsm2.model.Employee;
import com.example.hrsm2.model.PerformanceEvaluation;
import com.example.hrsm2.model.LeaveRequest;
import com.example.hrsm2.model.User;
import com.example.hrsm2.service.EmployeeService;
import com.example.hrsm2.service.PerformanceEvaluationService;
import com.example.hrsm2.service.LeaveRequestService;
import com.example.hrsm2.service.UserService;
import com.example.hrsm2.model.LeaveRequest.LeaveStatus;
import com.example.hrsm2.model.User.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
