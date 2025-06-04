package ru.utalieva.victorina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.utalieva.victorina.model.entity.FeedItem;
import ru.utalieva.victorina.model.entity.User;

import java.util.List;

@Repository
public interface FeedItemRepository extends JpaRepository<FeedItem, Long> {
    List<FeedItem> findByUserOrderByCompletedAtDesc(User user);
    boolean existsByUserAndQuizId(User user, Long quizId);
} 