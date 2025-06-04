package ru.utalieva.victorina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.utalieva.victorina.model.dto.FeedItemDTO;
import ru.utalieva.victorina.model.entity.FeedItem;
import ru.utalieva.victorina.service.FeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile/feed")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class FeedController {
    private final FeedService feedService;
    private static final Logger logger = LoggerFactory.getLogger(FeedController.class);

    @GetMapping
    public ResponseEntity<List<FeedItemDTO>> getUserFeed() {
        try {
            List<FeedItemDTO> feed = feedService.getUserFeed();
            return ResponseEntity.ok(feed);
        } catch (Exception e) {
            logger.error("Error fetching user feed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/publish")
    public ResponseEntity<?> publishQuizResult(@RequestBody FeedItemDTO feedItemDTO) {
        try {
            logger.info("Received request to publish quiz result: {}", feedItemDTO);
            
            // Validate required fields
            if (feedItemDTO.getQuizId() == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Quiz ID is required"));
            }
            
            if (feedItemDTO.getQuizTitle() == null || feedItemDTO.getQuizTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Quiz title is required"));
            }
            
            if (feedItemDTO.getQuizType() == null || feedItemDTO.getQuizType().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Quiz type is required"));
            }

            feedService.publishQuizResult(feedItemDTO);
            return ResponseEntity.ok()
                .body(Map.of("message", "Quiz result published successfully"));

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
                
        } catch (IllegalStateException e) {
            logger.warn("Business rule violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
                
        } catch (Exception e) {
            logger.error("Error publishing quiz result", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to publish quiz result: " + e.getMessage()));
        }
    }
} 