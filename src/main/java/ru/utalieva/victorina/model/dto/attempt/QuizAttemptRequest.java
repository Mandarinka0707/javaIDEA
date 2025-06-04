package ru.utalieva.victorina.model.dto.attempt;

import lombok.Data;
import java.util.Map;

@Data
public class QuizAttemptRequest {
    private Long quizId;
    private Long attemptId;  // ID текущей попытки
    private Map<Integer, Integer> answers; // questionIndex -> selectedOptionIndex
    private Integer timeSpent;
} 