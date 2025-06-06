package ru.utalieva.victorina.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import ru.utalieva.victorina.model.entity.User;
import ru.utalieva.victorina.model.entity.QuizAttempt;
import ru.utalieva.victorina.model.entity.UserRating;
import ru.utalieva.victorina.repository.UserRepository;
import ru.utalieva.victorina.repository.QuizAttemptRepository;
import ru.utalieva.victorina.repository.UserRatingRepository;
import ru.utalieva.victorina.service.UserRatingService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRatingServiceImpl implements UserRatingService {
    private static final Logger logger = LoggerFactory.getLogger(UserRatingServiceImpl.class);
    
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRatingRepository userRatingRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateUserRating(Long userId) {
        try {
            logger.debug("Starting to update rating for user {}", userId);
            
            List<QuizAttempt> attempts = quizAttemptRepository.findAllCompletedAttemptsExcludingPersonality(userId);
            
            UserRating rating = userRatingRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserRating newRating = new UserRating();
                    User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                    newRating.setUser(user);
                    return newRating;
                });

            if (attempts.isEmpty()) {
                rating.setAverageScore(BigDecimal.ZERO);
                rating.setTotalAttempts(0);
                rating.setCompletedQuizzes(0);
                userRatingRepository.save(rating);
                logger.debug("Updated rating for user {} with zero values (no attempts)", userId);
                return;
            }

            int totalScore = attempts.stream()
                .mapToInt(QuizAttempt::getScore)
                .sum();
                
            int totalPossibleScore = attempts.stream()
                .mapToInt(QuizAttempt::getTotalQuestions)
                .sum();

            double averageScore = totalPossibleScore > 0 
                ? ((double) totalScore / totalPossibleScore) * 100 
                : 0.0;

            rating.setAverageScore(BigDecimal.valueOf(averageScore).setScale(2, RoundingMode.HALF_UP));
            rating.setTotalAttempts(attempts.size());
            rating.setCompletedQuizzes((int) attempts.stream()
                .map(attempt -> attempt.getQuiz().getId())
                .distinct()
                .count());

            userRatingRepository.save(rating);
            logger.debug("Successfully updated rating for user {}: score={}, attempts={}, completed={}",
                userId, rating.getAverageScore(), rating.getTotalAttempts(), rating.getCompletedQuizzes());
                
        } catch (Exception e) {
            logger.error("Error updating rating for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to update user rating: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRating> getTopUsers(int limit) {
        return userRatingRepository.findAllOrderByAverageScoreDesc()
            .stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getUserRank(Long userId) {
        return userRatingRepository.getUserRank(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserRating getUserRating(Long userId) {
        return userRatingRepository.findByUserId(userId)
            .orElse(null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recalculateUserRating(Long userId) {
        try {
            updateUserRating(userId);
        } catch (Exception e) {
            logger.error("Failed to recalculate user rating for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
} 