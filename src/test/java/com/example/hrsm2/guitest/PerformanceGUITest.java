package com.example.hrsm2.guitest;

import com.example.hrsm2.gui.PerformanceGUI;
import com.example.hrsm2.model.Employee;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.assertions.api.Assertions.assertThat;

/**
 * GUI tests for the PerformanceGUI class.
 * Uses TestFX for automated UI testing.
 */
@ExtendWith(ApplicationExtension.class)
public class PerformanceGUITest {
    
    private TableView<Employee> tableView;
    private ComboBox<Employee> employeeComboBox;
    private DatePicker evaluationDatePicker;
    private Slider ratingSlider;
    private TextArea strengthsArea;
    private TextArea improvementArea;
    private TextArea commentsArea;
    private Button addButton;
    private Button updateButton;
    private Button deleteButton;
    private Button clearButton;
    
    /**
     * Will be called with {@code @Before} semantics, i.e. before each test method.
     *
     * @param stage - Will be injected by the test runner.
     */
    @Start
    private void start(Stage stage) throws Exception {
        PerformanceGUI performanceGUI = new PerformanceGUI();
        // Setup a scene with the PerformanceGUI
        // This would need to be adapted to your actual application's initialization process
        
        // For demonstration only; actual implementation would depend on your application structure
        // Scene scene = new Scene(performanceGUI.getRoot(), 800, 600);
        // stage.setScene(scene);
        // stage.show();
    }
    
    @BeforeEach
    public void setUp() {
        // Initialize test data or specific UI state
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        // Clean up resources
        FxToolkit.hideStage();
    }
    
    @Test
    public void testFormValidation(FxRobot robot) {
        // Click the add button without filling in required fields
        robot.clickOn("#addButton");
        
        // Verify that an error dialog appears
        assertThat(robot.lookup(".dialog-pane").queryAs(DialogPane.class)).isVisible();
        robot.clickOn(".dialog-pane .button");
    }
    
    @Test
    public void testAddEvaluation(FxRobot robot) {
        // Select an employee
        robot.clickOn("#employeeComboBox");
        robot.type(KeyCode.DOWN);
        robot.type(KeyCode.ENTER);
        
        // Set rating
        robot.moveTo("#ratingSlider");
        robot.drag().dropBy(50, 0);
        
        // Fill in text areas
        robot.clickOn("#strengthsArea");
        robot.write("Good communication skills");
        
        robot.clickOn("#improvementArea");
        robot.write("Could improve technical skills");
        
        robot.clickOn("#commentsArea");
        robot.write("Overall good performance");
        
        // Click add button
        robot.clickOn("#addButton");
        
        // Verify success dialog
        assertThat(robot.lookup(".dialog-pane").queryAs(DialogPane.class)).isVisible();
        assertTrue(robot.lookup(".dialog-pane .content").queryLabeled().getText()
            .contains("Performance evaluation added successfully"));
        
        // Close dialog
        robot.clickOn(".dialog-pane .button");
    }
    
    @Test
    public void testClearForm(FxRobot robot) {
        // Fill in form fields
        robot.clickOn("#strengthsArea");
        robot.write("Test strengths");
        
        robot.clickOn("#improvementArea");
        robot.write("Test improvements");
        
        // Click clear button
        robot.clickOn("#clearButton");
        
        // Verify fields are cleared
        assertThat(robot.lookup("#strengthsArea").queryAs(TextArea.class)).hasText("");
        assertThat(robot.lookup("#improvementArea").queryAs(TextArea.class)).hasText("");
    }
} 