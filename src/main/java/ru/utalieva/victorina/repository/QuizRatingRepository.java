package ru.utalieva.victorina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.utalieva.victorina.model.entity.QuizRating;
import java.util.Optional;

public interface QuizRatingRepository extends JpaRepository<QuizRating, Long> {
    Optional<QuizRating> findByQuizIdAndUserId(Long quizId, Long userId);

    @Query("SELECT AVG(r.rating) FROM QuizRating r WHERE r.quiz.id = :quizId")
    Double getAverageRatingByQuizId(@Param("quizId") Long quizId);

    @Query("SELECT COUNT(r) FROM QuizRating r WHERE r.quiz.id = :quizId")
    Integer getRatingCountByQuizId(@Param("quizId") Long quizId);
} 