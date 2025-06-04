package ru.utalieva.victorina.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.utalieva.victorina.model.dto.AuthRequest;
import ru.utalieva.victorina.model.dto.AuthResponse;
import ru.utalieva.victorina.model.dto.RegisterRequest;
import ru.utalieva.victorina.security.JwtTokenUtil;
import ru.utalieva.victorina.security.UserPrincipal;
import ru.utalieva.victorina.service.IUserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final IUserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            userService.register(request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Регистрация успешно завершена");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            logger.debug("Login attempt for user: {}", request.getUsername());
            String token = userService.login(request);
            Long userId = userService.getUserIdByUsername(request.getUsername());
            AuthResponse response = new AuthResponse(token, userId);
            logger.debug("Login successful for user: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login failed for user: {}", request.getUsername(), e);
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<Void> validateToken(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        logger.debug("Validating token for user: {}", userPrincipal != null ? userPrincipal.getUsername() : "null");
        if (userPrincipal != null) {
            logger.debug("Token validation successful for user: {}", userPrincipal.getUsername());
            return ResponseEntity.ok().build();
        }
        logger.warn("Token validation failed: user principal is null");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        logger.debug("Refreshing token for user: {}", userPrincipal != null ? userPrincipal.getUsername() : "null");
        if (userPrincipal != null) {
            String newToken = jwtTokenUtil.generateToken(userPrincipal);
            Map<String, Object> response = new HashMap<>();
            response.put("token", newToken);
            response.put("userId", userPrincipal.getId());
            logger.debug("Token refreshed successfully for user: {}", userPrincipal.getUsername());
            return ResponseEntity.ok(response);
        }
        logger.warn("Token refresh failed: user principal is null");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("Не удалось обновить токен"));
    }

    // DTO для ответа с ошибкой
    private static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}