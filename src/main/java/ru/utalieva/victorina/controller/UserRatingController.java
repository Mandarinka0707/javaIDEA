package ru.utalieva.victorina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.utalieva.victorina.model.entity.UserRating;
import ru.utalieva.victorina.security.UserPrincipal;
import ru.utalieva.victorina.service.UserRatingService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class UserRatingController {
    private final UserRatingService userRatingService;

    @GetMapping("/top")
    public ResponseEntity<List<Map<String, Object>>> getTopUsers() {
        List<UserRating> topUsers = userRatingService.getTopUsers(10);
        
        List<Map<String, Object>> response = topUsers.stream()
            .map(rating -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("username", rating.getUser().getUsername());
                userMap.put("averageScore", rating.getAverageScore());
                userMap.put("completedQuizzes", rating.getCompletedQuizzes());
                userMap.put("totalAttempts", rating.getTotalAttempts());
                return userMap;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyRating(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserRating rating = userRatingService.getUserRating(userPrincipal.getId());
        Integer rank = userRatingService.getUserRank(userPrincipal.getId());
        
        Map<String, Object> response = new HashMap<>();
        
        if (rating == null) {
            response.put("averageScore", 0);
            response.put("completedQuizzes", 0);
            response.put("totalAttempts", 0);
            response.put("rank", 0);
        } else {
            response.put("averageScore", rating.getAverageScore());
            response.put("completedQuizzes", rating.getCompletedQuizzes());
            response.put("totalAttempts", rating.getTotalAttempts());
            response.put("rank", rank);
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update/{userId}")
    public ResponseEntity<?> updateUserRating(@PathVariable Long userId) {
        userRatingService.updateUserRating(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshUserRating(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            userRatingService.recalculateUserRating(userPrincipal.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to refresh user rating: " + e.getMessage()));
        }
    }
} 