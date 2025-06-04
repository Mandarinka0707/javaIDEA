package ru.utalieva.victorina.service;

import ru.utalieva.victorina.model.dto.attempt.QuizAttemptRequest;
import ru.utalieva.victorina.model.dto.attempt.QuizAttemptResponse;
import ru.utalieva.victorina.security.UserPrincipal;

import java.util.List;
import java.util.Map;

public interface QuizAttemptService {
    boolean hasActiveAttempt(Long userId, Long quizId);
    
    QuizAttemptResponse startQuiz(Long userId, Long quizId);
    
    QuizAttemptResponse submitQuiz(Long userId, Long quizId, Long attemptId, Map<Integer, Integer> answers, Integer timeSpent);
    
    List<QuizAttemptResponse> getUserAttempts(Long userId);
    
    QuizAttemptResponse getActiveAttempt(Long quizId, UserPrincipal userPrincipal);
} 