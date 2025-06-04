package ru.utalieva.victorina.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.utalieva.victorina.model.dto.AuthRequest;
import ru.utalieva.victorina.model.dto.RegisterRequest;
import ru.utalieva.victorina.model.entity.User;

import java.util.List;
import java.util.Optional;

public interface IUserService extends UserDetailsService {
    void register(RegisterRequest request);
    String login(AuthRequest request);
    Long getUserIdByUsername(String username);
    User findById(Long id);
    List<User> searchUsers(String query);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User registerUser(String username, String password, String email);
} 