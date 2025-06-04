package ru.utalieva.victorina.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.utalieva.victorina.model.dto.FeedItemDTO;
import ru.utalieva.victorina.model.entity.FeedItem;
import ru.utalieva.victorina.model.entity.Quiz;
import ru.utalieva.victorina.model.entity.User;
import ru.utalieva.victorina.repository.FeedItemRepository;
import ru.utalieva.victorina.repository.QuizRepository;
import ru.utalieva.victorina.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedItemRepository feedItemRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private static final Logger logger = LoggerFactory.getLogger(FeedService.class);

    public List<FeedItemDTO> getUserFeed() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.debug("Fetching feed for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        List<FeedItemDTO> feed = feedItemRepository.findByUserOrderByCompletedAtDesc(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
        logger.debug("Found {} feed items for user: {}", feed.size(), username);
        return feed;
    }

    @Transactional
    public void publishQuizResult(FeedItemDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Publishing quiz result for user: {}. DTO: {}", username, dto);
        
        // Validate input
        if (dto.getQuizId() == null) {
            logger.error("Quiz ID is null in the request for user: {}", username);
            throw new IllegalArgumentException("Quiz ID cannot be null");
        }

        if (dto.getQuizTitle() == null || dto.getQuizTitle().trim().isEmpty()) {
            logger.error("Quiz title is null or empty in the request for user: {}", username);
            throw new IllegalArgumentException("Quiz title cannot be empty");
        }

        if (dto.getQuizType() == null || dto.getQuizType().trim().isEmpty()) {
            logger.error("Quiz type is null or empty in the request for user: {}", username);
            throw new IllegalArgumentException("Quiz type cannot be empty");
        }

        // Get user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found: {}", username);
                    return new RuntimeException("User not found: " + username);
                });
        logger.debug("Found user: {}", user.getUsername());

        // Get quiz
        Quiz quiz = quizRepository.findById(dto.getQuizId())
                .orElseThrow(() -> {
                    logger.error("Quiz not found with ID: {} for user: {}", dto.getQuizId(), username);
                    return new RuntimeException("Quiz not found with ID: " + dto.getQuizId());
                });
        logger.debug("Found quiz: {}", quiz.getTitle());

        // Check for duplicate
        if (feedItemRepository.existsByUserAndQuizId(user, quiz.getId())) {
            logger.warn("Quiz result already exists for user: {} and quiz: {}", username, dto.getQuizTitle());
            throw new IllegalStateException("Quiz result already published");
        }

        try {
            FeedItem feedItem = new FeedItem();
            feedItem.setUser(user);
            feedItem.setQuiz(quiz);
            feedItem.setQuizTitle(dto.getQuizTitle());
            feedItem.setQuizType(dto.getQuizType());
            feedItem.setCompletedAt(dto.getCompletedAt());
            feedItem.setScore(dto.getScore());
            feedItem.setTotalQuestions(dto.getTotalQuestions());
            feedItem.setTimeSpent(dto.getTimeSpent());
            feedItem.setPosition(dto.getPosition());
            feedItem.setCharacter(dto.getCharacter());
            feedItem.setDescription(dto.getDescription());
            feedItem.setImage(dto.getImage());
            feedItem.setTraits(dto.getTraits());

            logger.debug("Saving feed item: {}", feedItem);
            FeedItem savedItem = feedItemRepository.save(feedItem);
            logger.info("Successfully saved feed item with ID: {} for user: {}", savedItem.getId(), username);
        } catch (Exception e) {
            logger.error("Error saving feed item for user: {}. Error: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to save feed item: " + e.getMessage());
        }
    }

    private FeedItemDTO convertToDTO(FeedItem feedItem) {
        FeedItemDTO dto = new FeedItemDTO();
        dto.setId(feedItem.getId());
        dto.setQuizId(feedItem.getQuiz().getId());
        dto.setQuizTitle(feedItem.getQuizTitle());
        dto.setQuizType(feedItem.getQuizType());
        dto.setCompletedAt(feedItem.getCompletedAt());
        dto.setScore(feedItem.getScore());
        dto.setTotalQuestions(feedItem.getTotalQuestions());
        dto.setTimeSpent(feedItem.getTimeSpent());
        dto.setPosition(feedItem.getPosition());
        dto.setCharacter(feedItem.getCharacter());
        dto.setDescription(feedItem.getDescription());
        dto.setImage(feedItem.getImage());
        dto.setTraits(feedItem.getTraits());
        return dto;
    }
} 