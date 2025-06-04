package ru.utalieva.victorina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.utalieva.victorina.security.UserPrincipal;
import ru.utalieva.victorina.service.FriendshipService;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class FriendController {
    private final FriendshipService friendshipService;

    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getFriendCount(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        int count = friendshipService.getFriendCount(userPrincipal.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }
} 