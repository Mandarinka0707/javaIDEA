package ru.utalieva.victorina.model.dto;

import lombok.Data;
import java.util.Map;

@Data
public class QuizAttemptRequest {
    private Long quizId;
    private Map<Integer, Integer> answers; // questionIndex -> selectedOptionIndex
    private Integer timeSpent;
} 