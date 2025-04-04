package com.example.hrsm2.model;

import java.time.LocalDate;
import java.util.UUID;

public class PerformanceEvaluation {
    private String id;
    private String employeeId;
    private LocalDate evaluationDate;
    private int performanceRating; // 1-5 scale
    private String strengths;
    private String areasForImprovement;
    private String comments;
    private String reviewedBy;

    public PerformanceEvaluation() {
        this.id = UUID.randomUUID().toString();
        this.evaluationDate = LocalDate.now();
    }

    public PerformanceEvaluation(String employeeId, int performanceRating, String strengths, 
                                String areasForImprovement, String comments, String reviewedBy) {
        this();
        this.employeeId = employeeId;
        this.performanceRating = performanceRating;
        this.strengths = strengths;
        this.areasForImprovement = areasForImprovement;
        this.comments = comments;
        this.reviewedBy = reviewedBy;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(LocalDate evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    public int getPerformanceRating() {
        return performanceRating;
    }

    public void setPerformanceRating(int performanceRating) {
        this.performanceRating = performanceRating;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getAreasForImprovement() {
        return areasForImprovement;
    }

    public void setAreasForImprovement(String areasForImprovement) {
        this.areasForImprovement = areasForImprovement;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    // Method to get rating description
    public String getRatingDescription() {
        return switch (performanceRating) {
            case 1 -> "Poor";
            case 2 -> "Below Average";
            case 3 -> "Average";
            case 4 -> "Good";
            case 5 -> "Excellent";
            default -> "Not Rated";
        };
    }
} 