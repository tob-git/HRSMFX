package com.example.hrsm2.service;

import com.example.hrsm2.model.PerformanceEvaluation;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PerformanceEvaluationService {
    private Map<String, PerformanceEvaluation> evaluations;
    private static PerformanceEvaluationService instance;

    private PerformanceEvaluationService() {
        evaluations = new HashMap<>();
    }

    public static PerformanceEvaluationService getInstance() {
        if (instance == null) {
            instance = new PerformanceEvaluationService();
        }
        return instance;
    }

    public List<PerformanceEvaluation> getAllEvaluations() {
        return new ArrayList<>(evaluations.values());
    }

    public PerformanceEvaluation getEvaluationById(String id) {
        return evaluations.get(id);
    }

    public List<PerformanceEvaluation> getEvaluationsByEmployeeId(String employeeId) {
        return evaluations.values()
                .stream()
                .filter(evaluation -> evaluation.getEmployeeId().equals(employeeId))
                .collect(Collectors.toList());
    }

    public void addEvaluation(PerformanceEvaluation evaluation) {
        evaluations.put(evaluation.getId(), evaluation);
    }

    public void updateEvaluation(PerformanceEvaluation evaluation) {
        if (evaluations.containsKey(evaluation.getId())) {
            evaluations.put(evaluation.getId(), evaluation);
        }
    }

    public void deleteEvaluation(String id) {
        evaluations.remove(id);
    }

    public double getAverageRatingForEmployee(String employeeId) {
        List<PerformanceEvaluation> employeeEvaluations = getEvaluationsByEmployeeId(employeeId);
        
        if (employeeEvaluations.isEmpty()) {
            return 0.0;
        }
        
        int totalRating = 0;
        for (PerformanceEvaluation evaluation : employeeEvaluations) {
            totalRating += evaluation.getPerformanceRating();
        }
        
        return (double) totalRating / employeeEvaluations.size();
    }

    public List<PerformanceEvaluation> getEvaluationsByDateRange(LocalDate startDate, LocalDate endDate) {
        return evaluations.values()
                .stream()
                .filter(evaluation -> 
                    !evaluation.getEvaluationDate().isBefore(startDate) && 
                    !evaluation.getEvaluationDate().isAfter(endDate))
                .collect(Collectors.toList());
    }
} 