package ru.utalieva.victorina.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.utalieva.victorina.model.dto.feed.FeedItemDTO;
import ru.utalieva.victorina.model.entity.FeedItem;
import ru.utalieva.victorina.model.entity.Quiz;
import ru.utalieva.victorina.model.entity.QuizAttempt;
import ru.utalieva.victorina.model.entity.QuizResult;
import ru.utalieva.victorina.model.entity.User;
import ru.utalieva.victorina.repository.FeedItemRepository;
import ru.utalieva.victorina.repository.QuizRepository;
import ru.utalieva.victorina.repository.QuizResultRepository;
import ru.utalieva.victorina.repository.UserRepository;
import ru.utalieva.victorina.repository.QuizAttemptRepository;
import ru.utalieva.victorina.service.FeedService;
import ru.utalieva.victorina.service.FriendshipService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {
    private final FeedItemRepository feedItemRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final QuizResultRepository quizResultRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final FriendshipService friendshipService;

    @Override
    @Transactional(readOnly = true)
    public List<FeedItemDTO> getFriendsFeed(String username) {
        log.info("Getting friends feed for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        log.info("Found user: {} (ID: {})", user.getUsername(), user.getId());
        
        List<User> friends = friendshipService.getFriends(user);
        log.info("Found {} friends for user {}", friends.size(), username);
        
        List<Long> friendIds = friends.stream()
                .map(User::getId)
                .collect(Collectors.toList());
        log.info("Friend IDs: {}", friendIds);
        
        List<FeedItem> feedItems = feedItemRepository.findByUserIdInOrderByCompletedAtDesc(friendIds);
        log.info("Found {} feed items for friends", feedItems.size());
        
        List<FeedItemDTO> dtos = feedItems.stream()
                .map(FeedItemDTO::fromEntity)
                .collect(Collectors.toList());
        log.info("Converted {} feed items to DTOs", dtos.size());
        
        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedItemDTO> getFeedItems() {
        return feedItemRepository.findAllByOrderByCompletedAtDesc()
                .stream()
                .map(FeedItemDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedItemDTO> getUserFeedItems(Long userId) {
        return feedItemRepository.findByUserIdOrderByCompletedAtDesc(userId)
                .stream()
                .map(FeedItemDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FeedItemDTO publishQuizResult(FeedItemDTO dto) {
        if (dto.getQuizId() == null) {
            throw new IllegalArgumentException("Quiz ID is required");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Publishing quiz result for user: {}. DTO: {}", username, dto);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));

        Quiz quiz = quizRepository.findById(dto.getQuizId())
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found with ID: " + dto.getQuizId()));

        // Проверяем, нет ли уже такой публикации
        if (feedItemRepository.existsByUserAndQuizAndCompletedAt(user, quiz, dto.getCompletedAt())) {
            throw new IllegalStateException("Этот результат уже был опубликован");
        }

        FeedItem feedItem = new FeedItem();
        feedItem.setUser(user);
        feedItem.setQuiz(quiz);
        feedItem.setQuizTitle(dto.getQuizTitle());
        feedItem.setQuizType(dto.getQuizType());
        feedItem.setScore(dto.getScore());
        feedItem.setTotalQuestions(dto.getTotalQuestions());
        feedItem.setTimeSpent(dto.getTimeSpent());
        feedItem.setPosition(dto.getPosition());
        feedItem.setCompletedAt(dto.getCompletedAt() != null ? dto.getCompletedAt() : LocalDateTime.now());

        // Для личностного теста используем существующий результат из последней попытки
        if ("PERSONALITY".equals(dto.getQuizType())) {
            QuizAttempt lastAttempt = quizAttemptRepository.findFirstByUserIdAndQuizIdOrderByEndTimeDesc(
                user.getId(), quiz.getId())
                .orElseThrow(() -> new IllegalStateException("Quiz attempt not found"));
            
            if (lastAttempt.getPersonalityResult() != null) {
                QuizResult personalityResult = lastAttempt.getPersonalityResult();
                feedItem.setPersonalityResult(personalityResult);
                feedItem.setDescription(personalityResult.getDescription());
                feedItem.setImage(personalityResult.getImage());
                log.info("Using personality result from attempt: {}", personalityResult);
            } else {
                log.warn("No personality result found for quiz attempt");
            }
        }

        FeedItem savedItem = feedItemRepository.save(feedItem);
        log.info("Saved feed item: {}", savedItem);
        
        return FeedItemDTO.fromEntity(savedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedItemDTO> getGlobalFeed() {
        return feedItemRepository.findAllByOrderByCompletedAtDesc()
                .stream()
                .map(FeedItemDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteFeedItem(Long itemId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        FeedItem item = feedItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Feed item not found"));

        if (!item.getUser().getUsername().equals(username)) {
            throw new IllegalStateException("You can only delete your own feed items");
        }

        feedItemRepository.delete(item);
    }

    @Override
    @Transactional
    public FeedItemDTO publishQuizResult(Long quizId, QuizAttempt attempt) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        FeedItem feedItem = new FeedItem();
        feedItem.setUser(attempt.getUser());
        feedItem.setQuiz(quiz);
        feedItem.setQuizTitle(quiz.getTitle());
        feedItem.setQuizType(quiz.getType());
        feedItem.setScore(attempt.getScore());
        feedItem.setTotalQuestions(attempt.getTotalQuestions());
        feedItem.setTimeSpent(attempt.getTimeSpent());
        feedItem.setCompletedAt(attempt.getEndTime());
        
        // Добавляем результат личностного теста, если это тест типа PERSONALITY
        if ("PERSONALITY".equals(quiz.getType()) && attempt.getPersonalityResult() != null) {
            QuizResult personalityResult = attempt.getPersonalityResult();
            feedItem.setPersonalityResult(personalityResult);
            feedItem.setDescription(personalityResult.getDescription());
            feedItem.setImage(personalityResult.getImage());
            log.info("Setting personality result: {}", personalityResult);
        }

        feedItem = feedItemRepository.save(feedItem);
        return FeedItemDTO.fromEntity(feedItem);
    }
} 