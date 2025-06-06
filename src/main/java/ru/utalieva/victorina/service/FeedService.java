package ru.utalieva.victorina.service;

import ru.utalieva.victorina.model.dto.feed.FeedItemDTO;
import ru.utalieva.victorina.model.entity.QuizAttempt;

import java.util.List;

public interface FeedService {
    List<FeedItemDTO> getFriendsFeed(String username);
    
    List<FeedItemDTO> getFeedItems();
    
    List<FeedItemDTO> getUserFeedItems(Long userId);
    
    FeedItemDTO publishQuizResult(FeedItemDTO dto);
    
    List<FeedItemDTO> getGlobalFeed();
    
    void deleteFeedItem(Long itemId);
    
    FeedItemDTO publishQuizResult(Long quizId, QuizAttempt attempt);
} 