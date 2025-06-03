package ru.utalieva.victorina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.utalieva.victorina.model.entity.QuizAttempt;
import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserIdAndQuizId(Long userId, Long quizId);
    
    Optional<QuizAttempt> findByIdAndUserId(Long attemptId, Long userId);
    
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.isCompleted = true ORDER BY qa.score DESC, qa.timeSpent ASC")
    List<QuizAttempt> findTopAttemptsByQuizId(@Param("quizId") Long quizId);
    
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.user.id = :userId AND qa.isCompleted = true ORDER BY qa.endTime DESC")
    List<QuizAttempt> findCompletedAttemptsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(qa) + 1 FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.score > :score")
    Integer findPositionInRanking(@Param("quizId") Long quizId, @Param("score") Integer score);
} 