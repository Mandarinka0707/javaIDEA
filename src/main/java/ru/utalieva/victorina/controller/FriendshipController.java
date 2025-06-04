package ru.utalieva.victorina.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.utalieva.victorina.model.entity.User;
import ru.utalieva.victorina.security.UserPrincipal;
import ru.utalieva.victorina.service.FriendshipService;
import ru.utalieva.victorina.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashMap;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class FriendshipController {
    private static final Logger logger = LoggerFactory.getLogger(FriendshipController.class);
    
    private final FriendshipService friendshipService;
    private final UserService userService;

    @GetMapping("/status/{friendId}")
    public ResponseEntity<Map<String, String>> getFriendshipStatus(
            @PathVariable Long friendId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        try {
            if (userPrincipal == null) {
                logger.error("UserPrincipal is null");
                return ResponseEntity.badRequest().body(Map.of("status", "ERROR"));
            }

            logger.info("Checking friendship status - Current user ID: {}, Friend ID: {}", userPrincipal.getId(), friendId);
            
            User user = userService.findById(userPrincipal.getId());
            if (user == null) {
                logger.error("User not found for ID: {}", userPrincipal.getId());
                return ResponseEntity.badRequest().body(Map.of("status", "ERROR"));
            }
            logger.info("Current user found: {} (ID: {})", user.getUsername(), user.getId());
            
            boolean areFriends = friendshipService.areFriends(user.getId(), friendId);
            logger.info("Friendship status result - Are friends: {} (user {} and {})", areFriends, user.getId(), friendId);
            
            Map<String, String> response = Map.of("status", areFriends ? "ACCEPTED" : "NOT_FRIENDS");
            logger.info("Sending response: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking friendship status", e);
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR"));
        }
    }

    @PostMapping("/request/{friendId}")
    public ResponseEntity<?> sendFriendRequest(
            @PathVariable Long friendId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        try {
            User user = userService.findById(userPrincipal.getId());
            friendshipService.sendFriendRequest(user, friendId);
            return ResponseEntity.ok(Map.of("message", "Friend request sent successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "An error occurred while processing your request"));
        }
    }

    @PostMapping("/accept/{friendshipId}")
    public ResponseEntity<?> acceptFriendRequest(
            @PathVariable Long friendshipId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        User user = userService.findById(userPrincipal.getId());
        friendshipService.acceptFriendRequest(user, friendshipId);
        return ResponseEntity.ok(Map.of("message", "Friend request accepted"));
    }

    @PostMapping("/reject/{friendshipId}")
    public ResponseEntity<?> rejectFriendRequest(
            @PathVariable Long friendshipId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        User user = userService.findById(userPrincipal.getId());
        friendshipService.rejectFriendRequest(user, friendshipId);
        return ResponseEntity.ok(Map.of("message", "Friend request rejected"));
    }

    @GetMapping
    public ResponseEntity<List<User>> getFriends(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userService.findById(userPrincipal.getId());
        List<User> friends = friendshipService.getFriends(user);
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<User>> getPendingRequests(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userService.findById(userPrincipal.getId());
        List<User> pendingRequests = friendshipService.getPendingFriendRequests(user);
        return ResponseEntity.ok(pendingRequests);
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<?> removeFriend(
            @PathVariable Long friendId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        User user = userService.findById(userPrincipal.getId());
        friendshipService.removeFriend(user, friendId);
        return ResponseEntity.ok(Map.of("message", "Friend removed successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchUsers(
            @RequestParam String query,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        User currentUser = userService.findById(userPrincipal.getId());
        List<User> users = userService.searchUsers(query);
        
        List<Map<String, Object>> response = users.stream()
            .filter(user -> !user.getId().equals(currentUser.getId()))
            .map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("username", user.getUsername());
                userMap.put("isFriend", friendshipService.areFriends(currentUser.getId(), user.getId()));
                return userMap;
            })
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(response);
    }
} 