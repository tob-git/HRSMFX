package com.example.hrsm2.service;

import com.example.hrsm2.model.PerformanceEvaluation;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(OrderAnnotation.class)
class PerformanceEvaluationServiceTest {

private PerformanceEvaluationService operation;

@BeforeEach
void setUp() {
    operation = PerformanceEvaluationService.getInstance(); // Get singleton instance
}

@AfterEach
void tearDown() {
    // Clean up if needed (e.g., reset the list of evaluations)

}

@Test
@Order(1)
@DisplayName("1. Should return same instance for a singleton")
void getInstance() {
    PerformanceEvaluationService Test_instance = PerformanceEvaluationService.getInstance();
    // Check that both instances are the same (singleton pattern)
    assertSame(operation, Test_instance, "Both instances should be the same.");
}

@ParameterizedTest
@Order(2)
@DisplayName("2. Should add an evaluation")
@CsvSource({
        "emp1,4, Strengths1, Improvement1, Good, Manager",
        "emp2,5, Strengths2, Improvement2, Excellent, Manager",
        "emp3,1, Strengths3, Improvement3, Satisfactory, Manager",
        "emp4,4, Strengths4, Improvement4, Good, Manager",
        "emp5,5, Strengths5, Improvement5, Excellent, Manager",
        "emp6,3, Strengths6, Improvement6, Good, Manager"
})
void addEvaluation(String emp, int performanceRating, String strengths, String areasForImprovement, String comments, String reviewedBy) {
    // Create an evaluation
    PerformanceEvaluation eval = new PerformanceEvaluation(emp, performanceRating, strengths, areasForImprovement, comments, reviewedBy);

    // Add the evaluation
    operation.addEvaluation(eval);

    // Check if the evaluation was added
    PerformanceEvaluation result = operation.getEvaluationById(eval.getId());
    assertNotNull(result, "The evaluation should be added successfully.");
    assertEquals(eval.getId(), result.getId(), "The evaluation ID should match.");

    operation.deleteEvaluation(eval.getId());
}

@Test
@Order(3)
@DisplayName("3. Should get all evaluations")
void getAllEvaluations() {
    // Retrieve all evaluations
    List<PerformanceEvaluation> evaluations = operation.getAllEvaluations();

    // Validate the list size and content
    assertNotNull(evaluations, "Evaluations list should not be null.");
    assertTrue(evaluations.size() > 0, "There should be at least 1 evaluation."); // di el mafrood tefdal zy maheya keda mafish ta8iir.
}

@ParameterizedTest
@Order(4)
@DisplayName("4. Should get an evaluation by ID")
@CsvSource({
        "emp1,4, Strengths1, Improvement1, Good, Manager",
        "emp2,5, Strengths2, Improvement2, Excellent, Manager",
        "emp3,1, Strengths3, Improvement3, Satisfactory, Manager",
        "emp4,4, Strengths4, Improvement4, Good, Manager",
        "emp5,5, Strengths5, Improvement5, Excellent, Manager"
})
void getEvaluationById(String emp, int performanceRating, String strengths, String areasForImprovement, String comments, String reviewedBy) {
    // Create and add an evaluation
    PerformanceEvaluation eval = new PerformanceEvaluation(emp, performanceRating, strengths, areasForImprovement, comments, reviewedBy);
    operation.addEvaluation(eval);

    // Retrieve the evaluation by ID
    PerformanceEvaluation result = operation.getEvaluationById(eval.getId());

    // Validate the retrieved evaluation matches the one added
    assertNotNull(result, "Evaluation should be found.");
    assertEquals(eval.getId(), result.getId(), "The retrieved evaluation ID should match.");

    operation.deleteEvaluation(eval.getId());
}

@ParameterizedTest
@Order(5)
@DisplayName("5. Should get evaluations by employee ID")
@CsvSource({
        "emp1",
        "emp2",
        "emp3",
        "emp4",
        "emp5"
})
void getEvaluationsByEmployeeId(String employeeId) {
    // Create and add some evaluations
    PerformanceEvaluation eval1 = new PerformanceEvaluation(employeeId, 4, "Strengths", "Improvement", "Good", "Manager");
    PerformanceEvaluation eval2 = new PerformanceEvaluation(employeeId, 5, "Strengths", "Improvement", "Excellent", "Manager");
    operation.addEvaluation(eval1);
    operation.addEvaluation(eval2);

    // Retrieve evaluations by employee ID
    List<PerformanceEvaluation> result = operation.getEvaluationsByEmployeeId(employeeId);

    // Validate the list contains the correct number of evaluations for employee "emp3"
    assertNotNull(result, "Evaluations list should not be null.");
    assertEquals(2, result.size(), "There should be 2 evaluations for emp3.");

    operation.deleteEvaluation(eval1.getId());
    operation.deleteEvaluation(eval2.getId());
}

@ParameterizedTest
@Order(6)
@DisplayName("6. Should update an evaluation")
@CsvSource({
        "emp1,4, Strengths1, Improvement1, Good, Manager",
        "emp2,4, Strengths2, Improvement2, Excellent, Manager",
        "emp3,1, Strengths3, Improvement3, Satisfactory, Manager",
        "emp4,3, Strengths4, Improvement4, Good, Manager",
        "emp5,2, Strengths5, Improvement5, Excellent, Manager"
})
void updateEvaluation(String emp, int performanceRating, String strengths, String areasForImprovement, String comments, String reviewedBy) {
    // Create and add an evaluation
    PerformanceEvaluation eval = new PerformanceEvaluation(emp, performanceRating, strengths, areasForImprovement, comments, reviewedBy);
    operation.addEvaluation(eval);

    // Update the evaluation
    eval.setPerformanceRating(5);
    operation.updateEvaluation(eval);

    // Retrieve and check if the evaluation was updated
    PerformanceEvaluation updatedEval = operation.getEvaluationById(eval.getId());
    assertNotNull(updatedEval, "The evaluation should be updated successfully.");
    assertEquals(5, updatedEval.getPerformanceRating(), "The performance rating should be updated to 5.");
    operation.deleteEvaluation(eval.getId());
}

@ParameterizedTest
@Order(7)
@DisplayName("7. Should get the average rating for an employee")
@CsvSource({
        "emp1,4, Strengths1, Improvement1, Good, Manager",
        "emp2,4, Strengths2, Improvement2, Excellent, Manager",
        "emp3,1, Strengths3, Improvement3, Satisfactory, Manager",
        "emp4,3, Strengths4, Improvement4, Good, Manager",
        "emp5,2, Strengths5, Improvement5, Excellent, Manager"
})
void getAverageRatingForEmployee(String emp, int performanceRating, String strengths, String areasForImprovement, String comments, String reviewedBy) {
    // Create and add evaluations
    PerformanceEvaluation eval1 = new PerformanceEvaluation(emp, (performanceRating*8)%5, strengths, areasForImprovement, comments, reviewedBy);
    PerformanceEvaluation eval2 = new PerformanceEvaluation(emp, (performanceRating*3+1)%5, strengths, areasForImprovement, comments, reviewedBy);
    operation.addEvaluation(eval1);
    operation.addEvaluation(eval2);

    // Calculate average rating
    double expectedAvg = (eval1.getPerformanceRating() + eval2.getPerformanceRating()) / 2.0;
    double avgRating = operation.getAverageRatingForEmployee(emp);

    // Validate the average rating
    assertEquals(expectedAvg, avgRating, 0.01, "The average rating should be correctly calculated.");
    operation.deleteEvaluation(eval1.getId());
    operation.deleteEvaluation(eval2.getId());
}

@Test
@Order(8)
@DisplayName("8. Should get evaluations by date range")
void getEvaluationsByDateRange() {
    // Create and add evaluations with different dates
    PerformanceEvaluation eval1 = new PerformanceEvaluation("emp7", 4, "Strengths", "Improvement", "Good", "Manager");
    eval1.setEvaluationDate(LocalDate.of(2024, 1, 1));
    PerformanceEvaluation eval2 = new PerformanceEvaluation("emp7", 5, "Strengths", "Improvement", "Excellent", "Manager");
    eval2.setEvaluationDate(LocalDate.of(2023, 5, 1));
    operation.addEvaluation(eval1);
    operation.addEvaluation(eval2);

    // Get evaluations by date range
    List<PerformanceEvaluation> result = operation.getEvaluationsByDateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 4, 30));

    // Validate that only one evaluation is in the range
    assertNotNull(result, "Evaluations list should not be null.");
    assertEquals(1, result.size(), "There should be 1 evaluation in the date range.");

    operation.deleteEvaluation(eval1.getId());
    operation.deleteEvaluation(eval2.getId());
}

@Test
@Order(9)
@DisplayName("9. Should delete an evaluation")
void deleteEvaluation() {
    // Create and add an evaluation
    PerformanceEvaluation eval = new PerformanceEvaluation("emp5", 4, "Strengths", "Improvement", "Good", "Manager");
    operation.addEvaluation(eval);

    // Delete the evaluation
    operation.deleteEvaluation(eval.getId());

    // Check if the evaluation was deleted
    PerformanceEvaluation deletedEval = operation.getEvaluationById(eval.getId());
    assertNull(deletedEval, "The evaluation should be deleted.");

}


}
