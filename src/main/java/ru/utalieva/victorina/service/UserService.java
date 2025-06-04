package ru.utalieva.victorina.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.utalieva.victorina.model.entity.User;
import ru.utalieva.victorina.model.entity.UserRating;
import ru.utalieva.victorina.model.enumination.Role;
import ru.utalieva.victorina.repository.UserRepository;
import ru.utalieva.victorina.repository.UserRatingRepository;
import ru.utalieva.victorina.security.UserPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.utalieva.victorina.model.dto.AuthRequest;
import ru.utalieva.victorina.model.dto.RegisterRequest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRatingRepository userRatingRepository;

    public UserService(
            UserRepository userRepository,
            @Lazy PasswordEncoder passwordEncoder,
            UserRatingRepository userRatingRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRatingRepository = userRatingRepository;
    }

    @Transactional
    public User registerUser(String username, String password, String email) {
        logger.debug("Registering new user with username: {}", username);
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(Role.USER);
        user = userRepository.save(user);

        // Создаем начальный рейтинг для нового пользователя
        UserRating userRating = new UserRating();
        userRating.setUser(user);
        userRating.setAverageScore(BigDecimal.ZERO);
        userRating.setTotalAttempts(0);
        userRating.setCompletedQuizzes(0);
        userRatingRepository.save(userRating);

        logger.debug("User registered successfully: {}", user.getUsername());
        return user;
    }

    public boolean existsByUsername(String username) {
        logger.debug("Checking if username exists: {}", username);
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean existsByEmail(String email) {
        logger.debug("Checking if email exists: {}", email);
        return userRepository.findByEmail(email).isPresent();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return userRepository.findByUsernameContainingIgnoreCase(query.trim());
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void register(RegisterRequest request) {
        if (existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }
        if (existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(Role.USER);
        user = userRepository.save(user);

        // Создаем начальный рейтинг для нового пользователя
        UserRating userRating = new UserRating();
        userRating.setUser(user);
        userRating.setAverageScore(BigDecimal.ZERO);
        userRating.setTotalAttempts(0);
        userRating.setCompletedQuizzes(0);
        userRatingRepository.save(userRating);
    }

    public String login(AuthRequest request) {
        // Implementation of login method
        return null; // Placeholder return, actual implementation needed
    }

    public Long getUserIdByUsername(String username) {
        // Implementation of getUserIdByUsername method
        return null; // Placeholder return, actual implementation needed
    }
}