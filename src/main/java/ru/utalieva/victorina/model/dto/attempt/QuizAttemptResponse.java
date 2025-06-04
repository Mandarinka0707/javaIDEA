package ru.utalieva.victorina.model.dto.attempt;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizAttemptResponse {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private Integer score;
    private Integer totalQuestions;
    private Integer timeSpent;
    private Map<Integer, Integer> answers; // questionIndex -> selectedAnswer
    private List<String> correctAnswers; // Только для завершенной викторины
    private Boolean isCompleted;
    private Integer position; // Позиция в рейтинге
    private QuizResultDTO personalityResult; // Результат для personality quiz
    private LocalDateTime endTime; // Время завершения попытки
} 