package ru.utalieva.victorina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.utalieva.victorina.model.dto.QuizAttemptRequest;
import ru.utalieva.victorina.model.dto.QuizAttemptResponse;
import ru.utalieva.victorina.security.UserPrincipal;
import ru.utalieva.victorina.service.QuizAttemptService;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-attempts")
@RequiredArgsConstructor
public class QuizAttemptController {
    private final QuizAttemptService quizAttemptService;

    @PostMapping("/start/{quizId}")
    public ResponseEntity<QuizAttemptResponse> startQuiz(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long quizId) {
        QuizAttemptResponse response = quizAttemptService.startQuiz(userPrincipal.getId(), quizId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit")
    public ResponseEntity<QuizAttemptResponse> submitQuiz(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody QuizAttemptRequest request) {
        QuizAttemptResponse response = quizAttemptService.submitQuiz(userPrincipal.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<QuizAttemptResponse>> getUserAttempts(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<QuizAttemptResponse> attempts = quizAttemptService.getUserAttempts(userPrincipal.getId());
        return ResponseEntity.ok(attempts);
    }
} 