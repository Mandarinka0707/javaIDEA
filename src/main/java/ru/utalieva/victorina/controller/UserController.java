package ru.utalieva.victorina.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.utalieva.victorina.model.entity.User;
import ru.utalieva.victorina.security.UserPrincipal;
import ru.utalieva.victorina.service.IUserService;
import ru.utalieva.victorina.service.UserRatingService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final IUserService userService;
    private final UserRatingService userRatingService;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            logger.debug("Getting profile for user ID: {}", userPrincipal.getId());
            User user = userService.findById(userPrincipal.getId());
            
            if (user == null) {
                logger.error("User not found for ID: {}", userPrincipal.getId());
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("username", user.getUsername());
            profile.put("email", user.getEmail());
            
            // Добавляем рейтинг пользователя
            var rating = userRatingService.getUserRating(user.getId());
            if (rating != null) {
                logger.debug("Found rating for user {}: score={}, completed={}, attempts={}", 
                    user.getUsername(), rating.getAverageScore(), rating.getCompletedQuizzes(), rating.getTotalAttempts());
                profile.put("rating", rating.getAverageScore());
                profile.put("completedQuizzes", rating.getCompletedQuizzes());
                profile.put("totalAttempts", rating.getTotalAttempts());
            } else {
                logger.debug("No rating found for user {}, using default values", user.getUsername());
                profile.put("rating", 0.0);
                profile.put("completedQuizzes", 0);
                profile.put("totalAttempts", 0);
            }
            
            logger.debug("Returning profile for user {}: {}", user.getUsername(), profile);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("Error getting user profile", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 