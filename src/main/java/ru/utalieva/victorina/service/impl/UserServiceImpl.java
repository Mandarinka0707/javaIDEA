package ru.utalieva.victorina.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.utalieva.victorina.model.dto.AuthRequest;
import ru.utalieva.victorina.model.dto.RegisterRequest;
import ru.utalieva.victorina.model.entity.User;
import ru.utalieva.victorina.model.entity.UserRating;
import ru.utalieva.victorina.model.enumination.Role;
import ru.utalieva.victorina.repository.UserRepository;
import ru.utalieva.victorina.repository.UserRatingRepository;
import ru.utalieva.victorina.security.JwtTokenUtil;
import ru.utalieva.victorina.security.UserPrincipal;
import ru.utalieva.victorina.service.IUserService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRatingRepository userRatingRepository;

    public UserServiceImpl(
            UserRepository userRepository,
            @Lazy PasswordEncoder passwordEncoder,
            @Lazy AuthenticationManager authenticationManager,
            @Lazy JwtTokenUtil jwtTokenUtil,
            UserRatingRepository userRatingRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRatingRepository = userRatingRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return new UserPrincipal(user);
    }

    @Override
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

    @Override
    public String login(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return jwtTokenUtil.generateToken(userDetails);
        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException("Неверное имя пользователя или пароль");
        }
    }

    @Override
    public Long getUserIdByUsername(String username) {
        return findByUsername(username)
            .map(User::getId)
            .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    @Override
    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return userRepository.findByUsernameContainingIgnoreCase(query.trim());
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User registerUser(String username, String password, String email) {
        if (existsByUsername(username)) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }
        if (existsByEmail(email)) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

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

        return user;
    }
} 