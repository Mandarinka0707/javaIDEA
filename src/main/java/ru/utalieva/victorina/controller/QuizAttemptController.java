package ru.utalieva.victorina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.utalieva.victorina.model.dto.attempt.QuizAttemptRequest;
import ru.utalieva.victorina.model.dto.attempt.QuizAttemptResponse;
import ru.utalieva.victorina.security.UserPrincipal;
import ru.utalieva.victorina.service.QuizAttemptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("/api/quiz-attempts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class QuizAttemptController {
    private final QuizAttemptService quizAttemptService;
    private static final Logger logger = LoggerFactory.getLogger(QuizAttemptController.class);
    private static final ConcurrentHashMap<String, Lock> locks = new ConcurrentHashMap<>();

    @GetMapping("/my")
    public ResponseEntity<List<QuizAttemptResponse>> getUserAttempts(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            List<QuizAttemptResponse> attempts = quizAttemptService.getUserAttempts(userPrincipal.getId());
            return ResponseEntity.ok(attempts);
        } catch (Exception e) {
            logger.error("Error getting user attempts", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/start/{quizId}")
    @Transactional
    public ResponseEntity<?> startQuiz(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long quizId) {
        String lockKey = userPrincipal.getId() + ":" + quizId;
        Lock lock = locks.computeIfAbsent(lockKey, k -> new ReentrantLock());
        
        if (!lock.tryLock()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Запрос на начало викторины уже обрабатывается"));
        }
        
        try {
            // Проверяем, нет ли уже активной попытки
            if (quizAttemptService.hasActiveAttempt(userPrincipal.getId(), quizId)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "У вас уже есть активная попытка для этой викторины"));
            }

            QuizAttemptResponse attempt = quizAttemptService.startQuiz(userPrincipal.getId(), quizId);
            logger.info("Started quiz attempt {} for user {} on quiz {}", 
                attempt.getAttemptId(), userPrincipal.getId(), quizId);
            return ResponseEntity.ok(attempt);
        } catch (Exception e) {
            logger.error("Error starting quiz", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Ошибка при начале викторины: " + e.getMessage()));
        } finally {
            lock.unlock();
            // Очищаем блокировку если она больше не нужна
            if (!quizAttemptService.hasActiveAttempt(userPrincipal.getId(), quizId)) {
                locks.remove(lockKey);
            }
        }
    }

    @PostMapping("/submit")
    @Transactional
    public ResponseEntity<?> submitQuiz(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody QuizAttemptRequest request) {
        String lockKey = userPrincipal.getId() + ":" + request.getQuizId();
        Lock lock = locks.computeIfAbsent(lockKey, k -> new ReentrantLock());
        
        if (!lock.tryLock()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Запрос на завершение викторины уже обрабатывается"));
        }
        
        try {
            logger.info("Submitting quiz attempt {} for user {}", 
                request.getAttemptId(), userPrincipal.getId());

            QuizAttemptResponse response = quizAttemptService.submitQuiz(
                userPrincipal.getId(), 
                request.getQuizId(),
                request.getAttemptId(),
                request.getAnswers(),
                request.getTimeSpent()
            );

            logger.info("Successfully submitted quiz attempt {} for user {}", 
                request.getAttemptId(), userPrincipal.getId());
                
            return ResponseEntity.ok()
                .body(Map.of(
                    "message", "Викторина успешно завершена",
                    "result", response
                ));
        } catch (IllegalStateException e) {
            logger.warn("Invalid quiz attempt state: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error submitting quiz", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Ошибка при отправке ответов: " + e.getMessage()));
        } finally {
            lock.unlock();
            locks.remove(lockKey);
        }
    }

    @GetMapping("/my/active/{quizId}")
    public ResponseEntity<QuizAttemptResponse> getActiveAttempt(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        QuizAttemptResponse attempt = quizAttemptService.getActiveAttempt(quizId, userPrincipal);
        return ResponseEntity.ok(attempt);
    }
} 