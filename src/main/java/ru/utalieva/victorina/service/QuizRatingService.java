package ru.utalieva.victorina.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.utalieva.victorina.model.entity.Quiz;
import ru.utalieva.victorina.model.entity.QuizRating;
import ru.utalieva.victorina.model.entity.User;
import ru.utalieva.victorina.repository.QuizRatingRepository;
import ru.utalieva.victorina.repository.QuizRepository;
import ru.utalieva.victorina.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuizRatingService {
    private static final Logger logger = LoggerFactory.getLogger(QuizRatingService.class);
    private final QuizRatingRepository quizRatingRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;

    @Transactional
    public QuizRating rateQuiz(Long quizId, Long userId, Integer rating) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Викторина не найдена"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверяем, оценивал ли пользователь эту викторину ранее
        Optional<QuizRating> existingRating = quizRatingRepository.findByQuizIdAndUserId(quizId, userId);
        if (existingRating.isPresent()) {
            throw new IllegalStateException("Вы уже оценили эту викторину");
        }

        // Создаем новую оценку
        QuizRating quizRating = new QuizRating();
        quizRating.setQuiz(quiz);
        quizRating.setUser(user);
        quizRating.setRating(rating);

        quizRating = quizRatingRepository.save(quizRating);

        // Получаем актуальные данные для расчета среднего
        Double averageRating = quizRatingRepository.getAverageRatingByQuizId(quizId);
        Integer ratingCount = quizRatingRepository.getRatingCountByQuizId(quizId);

        // Обновляем среднюю оценку и количество оценок в викторине
        quiz.setAverageRating(averageRating != null ? 
            BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO);
        quiz.setRatingCount(ratingCount != null ? ratingCount : 0);

        quizRepository.save(quiz);
        logger.info("Updated quiz rating - Quiz: {}, Average: {}, Count: {}", 
            quiz.getId(), quiz.getAverageRating(), quiz.getRatingCount());

        return quizRating;
    }

    @Transactional(readOnly = true)
    public QuizRating getUserRating(Long quizId, Long userId) {
        return quizRatingRepository.findByQuizIdAndUserId(quizId, userId)
                .orElse(null);
    }

    private void updateQuizRating(Quiz quiz) {
        Double averageRating = quizRatingRepository.getAverageRatingByQuizId(quiz.getId());
        Integer ratingCount = quizRatingRepository.getRatingCountByQuizId(quiz.getId());

        quiz.setAverageRating(averageRating != null ? 
            BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO);
        quiz.setRatingCount(ratingCount != null ? ratingCount : 0);

        quizRepository.save(quiz);
        logger.info("Updated quiz rating - Quiz: {}, Average: {}, Count: {}", 
            quiz.getId(), quiz.getAverageRating(), quiz.getRatingCount());
    }
} 