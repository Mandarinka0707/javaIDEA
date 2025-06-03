package ru.utalieva.victorina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.utalieva.victorina.model.entity.QuizResult;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    // Дополнительные методы можно добавить здесь
} 