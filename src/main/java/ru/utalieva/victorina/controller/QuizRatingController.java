package ru.utalieva.victorina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.utalieva.victorina.model.entity.QuizRating;
import ru.utalieva.victorina.security.UserPrincipal;
import ru.utalieva.victorina.service.QuizRatingService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@Validated
@SuppressWarnings("unused")
public class QuizRatingController {
    private final QuizRatingService quizRatingService;

    @PostMapping("/{quizId}/rate")
    @SuppressWarnings("unused")
    public ResponseEntity<Map<String, Object>> rateQuiz(
            @PathVariable @NonNull Long quizId,
            @RequestBody @NonNull Map<String, Integer> request,
            @AuthenticationPrincipal @NonNull UserPrincipal userPrincipal) {
        
        Integer rating = request.get("rating");
        if (rating == null || rating < 1 || rating > 5) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Оценка должна быть от 1 до 5"));
        }

        try {
            QuizRating quizRating = quizRatingService.rateQuiz(quizId, userPrincipal.getId(), rating);
            
            Map<String, Object> response = new HashMap<>();
            response.put("averageRating", quizRating.getQuiz().getAverageRating());
            response.put("totalRatings", quizRating.getQuiz().getRatingCount());
            
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409)
                .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("message", "Произошла ошибка при сохранении оценки"));
        }
    }

    @GetMapping("/{quizId}/user-rating")
    @SuppressWarnings("unused")
    public ResponseEntity<Map<String, Object>> getUserRating(
            @PathVariable @NonNull Long quizId,
            @AuthenticationPrincipal @NonNull UserPrincipal userPrincipal) {
        
        QuizRating rating = quizRatingService.getUserRating(quizId, userPrincipal.getId());
        
        Map<String, Object> response = new HashMap<>();
        if (rating == null) {
            response.put("rating", null);
        } else {
            response.put("rating", rating.getRating());
        }
        
        return ResponseEntity.ok(response);
    }
} 