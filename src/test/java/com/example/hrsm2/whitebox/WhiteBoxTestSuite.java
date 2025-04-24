package com.example.hrsm2.whitebox;

import com.example.hrsm2.whitebox.*;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite that combines all whitebox test classes.
 * This suite will run all the whitebox tests in a single execution.
 */
@Suite
@SuiteDisplayName("WhiteBox Test Suite")
@SelectClasses({
    LoginControllerWhiteBoxTest.class,
    UserControllerWhiteBoxTest.class,
    LeaveControllerWhiteBoxTest.class,
    PerformanceControllerWhiteBoxTest.class,
    EmployeeControllerWhiteBoxTest.class,
    PayrollControllerWhiteBoxTest.class
})
public class WhiteBoxTestSuite {
    // This class remains empty, it is used only as a holder for the above annotations
} 