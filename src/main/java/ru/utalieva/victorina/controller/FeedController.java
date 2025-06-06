package ru.utalieva.victorina.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.utalieva.victorina.model.dto.feed.FeedItemDTO;
import ru.utalieva.victorina.service.FeedService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feed")
public class FeedController {
    private final FeedService feedService;

    @GetMapping("/friends")
    public ResponseEntity<List<FeedItemDTO>> getFriendsFeed(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Getting friends feed for user: {}", userDetails.getUsername());
        return ResponseEntity.ok(feedService.getFriendsFeed(userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<FeedItemDTO>> getFeed() {
        return ResponseEntity.ok(feedService.getFeedItems());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FeedItemDTO>> getUserFeed(@PathVariable Long userId) {
        return ResponseEntity.ok(feedService.getUserFeedItems(userId));
    }

    @PostMapping("/publish")
    public ResponseEntity<?> publishQuizResult(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody FeedItemDTO feedItemDTO) {
        try {
            log.info("Publishing quiz result for user: {}", userDetails.getUsername());
            log.info("Feed item data: {}", feedItemDTO);
            
            if (feedItemDTO.getQuizType().equals("PERSONALITY")) {
                log.info("Personality result data: {}", feedItemDTO.getPersonalityResult());
            }
            
            FeedItemDTO published = feedService.publishQuizResult(feedItemDTO);
            return ResponseEntity.ok(published);
        } catch (IllegalArgumentException e) {
            log.error("Invalid feed item data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("Feed item already exists: {}", e.getMessage());
            return ResponseEntity.status(409)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error publishing quiz result", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Не удалось опубликовать результат"));
        }
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteFeedItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {
        try {
            feedService.deleteFeedItem(itemId);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "У вас нет прав для удаления этой публикации"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound()
                    .build();
        }
    }
} 