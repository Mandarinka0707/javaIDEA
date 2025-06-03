package ru.utalieva.victorina.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import ru.utalieva.victorina.model.dto.JwtResponse;
import ru.utalieva.victorina.model.dto.LoginRequest;
import ru.utalieva.victorina.model.dto.RegisterRequest;
import ru.utalieva.victorina.model.entity.User;
import ru.utalieva.victorina.security.JwtTokenUtil;
import ru.utalieva.victorina.service.UserService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          JwtTokenUtil jwtTokenUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            logger.info("Attempting login for user: {}", request.getUsername());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtTokenUtil.generateToken(userDetails);
            
            logger.info("Login successful for user: {}", request.getUsername());
            return ResponseEntity.ok(new JwtResponse(token));
            
        } catch (BadCredentialsException e) {
            logger.warn("Login failed for user: {} - Bad credentials", request.getUsername());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Неверное имя пользователя или пароль"));
        } catch (UsernameNotFoundException e) {
            logger.warn("Login failed - User not found: {}", request.getUsername());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Пользователь не найден"));
        } catch (Exception e) {
            logger.error("Login failed for user: {} - {}", request.getUsername(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Произошла ошибка при входе в систему"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            logger.info("Attempting registration for user: {}", request.getUsername());
            
            // Проверяем, существует ли пользователь
            if (userService.existsByUsername(request.getUsername())) {
                logger.warn("Registration failed - Username already exists: {}", request.getUsername());
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Пользователь с таким именем уже существует"));
            }
            
            // Проверяем, существует ли email
            if (userService.existsByEmail(request.getEmail())) {
                logger.warn("Registration failed - Email already exists: {}", request.getEmail());
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Пользователь с таким email уже существует"));
            }

            User user = userService.registerUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getEmail()
            );
            
            logger.info("Registration successful for user: {}", request.getUsername());
            return ResponseEntity.ok(user);
            
        } catch (Exception e) {
            logger.error("Registration failed for user: {} - {}", request.getUsername(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Произошла ошибка при регистрации"));
        }
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