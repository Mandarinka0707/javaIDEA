package ru.utalieva.victorina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.utalieva.victorina.model.entity.Quiz;
import ru.utalieva.victorina.model.entity.User;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByCreator(User creator);
} 