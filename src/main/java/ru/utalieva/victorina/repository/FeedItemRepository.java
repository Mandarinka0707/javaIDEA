package ru.utalieva.victorina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.utalieva.victorina.model.entity.FeedItem;
import ru.utalieva.victorina.model.entity.Quiz;
import ru.utalieva.victorina.model.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface FeedItemRepository extends JpaRepository<FeedItem, Long> {
    @Query("SELECT f FROM FeedItem f LEFT JOIN FETCH f.personalityResult LEFT JOIN FETCH f.user WHERE f.user.username = :username ORDER BY f.createdAt DESC")
    List<FeedItem> findByUserUsernameOrderByCreatedAtDesc(@Param("username") String username);

    @Query("SELECT f FROM FeedItem f LEFT JOIN FETCH f.personalityResult LEFT JOIN FETCH f.user ORDER BY f.createdAt DESC")
    List<FeedItem> findAllByOrderByCreatedAtDesc();

    boolean existsByUserAndQuizAndCompletedAt(User user, Quiz quiz, LocalDateTime completedAt);

    List<FeedItem> findAllByOrderByCompletedAtDesc();
    List<FeedItem> findByUserIdOrderByCompletedAtDesc(Long userId);

    @Query("SELECT f FROM FeedItem f LEFT JOIN FETCH f.personalityResult LEFT JOIN FETCH f.user WHERE f.user.id IN :userIds ORDER BY f.completedAt DESC")
    List<FeedItem> findByUserIdInOrderByCompletedAtDesc(@Param("userIds") List<Long> userIds);
} 