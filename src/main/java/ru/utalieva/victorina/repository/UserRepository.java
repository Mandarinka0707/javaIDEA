package ru.utalieva.victorina.repository;

import ru.utalieva.victorina.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByUsernameContainingIgnoreCase(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}


