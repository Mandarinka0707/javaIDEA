package ru.utalieva.victorina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.utalieva.victorina.model.entity.QuizAttempt;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserIdAndQuizId(Long userId, Long quizId);
    
    @Query("SELECT qa FROM QuizAttempt qa " +
           "WHERE qa.user.id = :userId " +
           "AND qa.isCompleted = true " +
           "ORDER BY qa.quiz.id ASC, qa.endTime DESC")
    List<QuizAttempt> findCompletedAttemptsByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM quiz_attempts " +
           "WHERE user_id = :userId " +
           "AND quiz_id = :quizId " +
           "AND is_completed = true " +
           "ORDER BY end_time DESC", 
           nativeQuery = true)
    List<QuizAttempt> findAllAttemptsByUserIdAndQuizId(@Param("userId") Long userId, @Param("quizId") Long quizId);

    @Query(value = """
           SELECT * FROM quiz_attempts qa
           WHERE qa.user_id = :userId 
           AND qa.is_completed = true
           AND qa.score IS NOT NULL
           AND qa.total_questions > 0
           ORDER BY qa.quiz_id ASC, qa.end_time DESC
           """, 
           nativeQuery = true)
    List<QuizAttempt> findAllCompletedAttempts(@Param("userId") Long userId);

    @Query(value = """
           SELECT * FROM quiz_attempts 
           WHERE user_id = :userId 
           AND quiz_id = :quizId 
           AND is_completed = false 
           AND start_time > NOW() - INTERVAL '24 HOUR'
           ORDER BY start_time DESC 
           LIMIT 1
           """, 
           nativeQuery = true)
    Optional<QuizAttempt> findByUserIdAndQuizIdAndIsCompletedFalse(
            @Param("userId") Long userId, 
            @Param("quizId") Long quizId);
    
    @Query("SELECT qa FROM QuizAttempt qa " +
           "WHERE qa.user.id = :userId " +
           "AND qa.isCompleted = true " +
           "AND qa.score IS NOT NULL " +
           "AND qa.totalQuestions > :minQuestions " +
           "ORDER BY qa.quiz.id ASC, qa.endTime DESC")
    List<QuizAttempt> findByUserIdAndIsCompletedTrueAndScoreIsNotNullAndTotalQuestionsGreaterThanOrderByQuizIdAscEndTimeDesc(
            @Param("userId") Long userId, @Param("minQuestions") int minQuestions);

    @Query(value = """
           SELECT * FROM quiz_attempts qa 
           WHERE qa.quiz_id = :quizId 
           AND qa.user_id = :userId 
           AND qa.is_completed = false 
           AND qa.start_time > NOW() - INTERVAL '24 HOUR'
           ORDER BY qa.start_time DESC 
           LIMIT 1
           """, 
           nativeQuery = true)
    Optional<QuizAttempt> findActiveAttemptByQuizIdAndUserId(
            @Param("quizId") Long quizId,
            @Param("userId") Long userId
    );

    Optional<QuizAttempt> findFirstByUserIdAndQuizIdOrderByEndTimeDesc(Long userId, Long quizId);

    @Query("SELECT qa FROM QuizAttempt qa " +
           "WHERE qa.user.id = :userId " +
           "AND qa.isCompleted = true " +
           "AND qa.quiz.quizType != 'PERSONALITY' " +
           "ORDER BY qa.endTime DESC")
    List<QuizAttempt> findAllCompletedAttemptsExcludingPersonality(@Param("userId") Long userId);
} 