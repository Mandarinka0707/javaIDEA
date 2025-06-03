package ru.utalieva.victorina.model.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class QuizAttemptResponse {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private Integer score;
    private Integer totalQuestions;
    private Integer timeSpent;
    private Map<Integer, Boolean> answers; // questionIndex -> isCorrect
    private List<String> correctAnswers; // Только для завершенной викторины
    private Boolean isCompleted;
    private Integer position; // Позиция в рейтинге
    private QuizResultDTO personalityResult; // Результат для personality quiz
} 