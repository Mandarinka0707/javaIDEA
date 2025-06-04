package ru.utalieva.victorina.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import ru.utalieva.victorina.model.dto.attempt.QuizAttemptResponse;
import ru.utalieva.victorina.model.dto.attempt.QuizResultDTO;
import ru.utalieva.victorina.model.entity.Quiz;
import ru.utalieva.victorina.model.entity.QuizAttempt;
import ru.utalieva.victorina.model.entity.QuizResult;
import ru.utalieva.victorina.model.entity.Question;
import ru.utalieva.victorina.model.entity.Option;
import ru.utalieva.victorina.model.entity.User;
import ru.utalieva.victorina.model.enumination.QuizType;
import ru.utalieva.victorina.repository.QuizAttemptRepository;
import ru.utalieva.victorina.repository.QuizRepository;
import ru.utalieva.victorina.repository.UserRepository;
import ru.utalieva.victorina.security.UserPrincipal;
import ru.utalieva.victorina.service.QuizAttemptService;
import ru.utalieva.victorina.service.UserRatingService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class QuizAttemptServiceImpl implements QuizAttemptService {
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final UserRatingService userRatingService;
    private static final Logger logger = LoggerFactory.getLogger(QuizAttemptServiceImpl.class);

    @Override
    public boolean hasActiveAttempt(Long userId, Long quizId) {
        return quizAttemptRepository.findByUserIdAndQuizIdAndIsCompletedFalse(userId, quizId)
            .isPresent();
    }

    @Override
    @Transactional
    public QuizAttemptResponse startQuiz(Long userId, Long quizId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // Проверяем наличие активной попытки
        if (hasActiveAttempt(userId, quizId)) {
            throw new IllegalStateException("У вас уже есть активная попытка для этой викторины");
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setQuiz(quiz);
        attempt.setStartTime(LocalDateTime.now());
        attempt.setIsCompleted(false);
        attempt.setScore(0);
        attempt.setTotalQuestions(quiz.getQuestions().size());
        attempt.setTimeSpent(0);

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);
        
        return QuizAttemptResponse.builder()
            .attemptId(savedAttempt.getId())
            .quizId(quiz.getId())
            .quizTitle(quiz.getTitle())
            .score(0)
            .totalQuestions(quiz.getQuestions().size())
            .timeSpent(0)
            .isCompleted(false)
            .build();
    }

    @Override
    @Transactional
    public QuizAttemptResponse submitQuiz(
            Long userId, 
            Long quizId,
            Long attemptId,
            Map<Integer, Integer> answers,
            Integer timeSpent) {
        
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
            .orElseThrow(() -> new IllegalStateException("Попытка не найдена"));

        if (!attempt.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Попытка принадлежит другому пользователю");
        }

        if (!attempt.getQuiz().getId().equals(quizId)) {
            throw new IllegalStateException("Попытка для другой викторины");
        }

        if (attempt.getIsCompleted()) {
            throw new IllegalStateException("Попытка уже завершена");
        }

        Quiz quiz = attempt.getQuiz();
        
        if (quiz.getQuizType() == QuizType.PERSONALITY) {
            // Для personality викторины определяем результат на основе ответов
            QuizResult personalityResult = calculatePersonalityResult(quiz, answers);
            attempt.setPersonalityResult(personalityResult);
            attempt.setScore(answers.size()); // Просто для записи количества отвеченных вопросов
        } else {
            // Для обычной викторины считаем правильные ответы
            int score = calculateScore(quiz, answers);
            attempt.setScore(score);
        }
        
        // Обновляем попытку
        attempt.setTimeSpent(timeSpent);
        attempt.setEndTime(LocalDateTime.now());
        attempt.setIsCompleted(true);
        attempt.setUserAnswers(answers);

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        // Обновляем рейтинг пользователя в отдельной транзакции
        updateUserRatingAsync(userId);

        return QuizAttemptResponse.builder()
            .attemptId(savedAttempt.getId())
            .quizId(savedAttempt.getQuiz().getId())
            .quizTitle(savedAttempt.getQuiz().getTitle())
            .score(savedAttempt.getScore())
            .totalQuestions(savedAttempt.getTotalQuestions())
            .timeSpent(timeSpent)
            .isCompleted(true)
            .answers(answers)
            .endTime(savedAttempt.getEndTime())
            .personalityResult(savedAttempt.getPersonalityResult() != null ? 
                QuizResultDTO.fromEntity(savedAttempt.getPersonalityResult()) : null)
            .build();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void updateUserRatingAsync(Long userId) {
        try {
            userRatingService.recalculateUserRating(userId);
        } catch (Exception e) {
            // Log the error but don't fail the quiz submission
            logger.error("Failed to update user rating for user {}: {}", userId, e.getMessage(), e);
        }
    }

    private int calculateScore(Quiz quiz, Map<Integer, Integer> answers) {
        // Для викторин типа PERSONALITY не нужно считать очки
        if (quiz.getQuizType() == QuizType.PERSONALITY) {
            return answers.size(); // Возвращаем количество отвеченных вопросов как счет
        }

        // Для обычных викторин считаем правильные ответы
        int score = 0;
        for (Map.Entry<Integer, Integer> entry : answers.entrySet()) {
            int questionIndex = entry.getKey();
            int selectedAnswer = entry.getValue();
            
            if (questionIndex < quiz.getQuestions().size()) {
                var question = quiz.getQuestions().get(questionIndex);
                if (question.getCorrectIndex() != null && question.getCorrectIndex() == selectedAnswer) {
                    score++;
                }
            }
        }
        return score;
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizAttemptResponse> getUserAttempts(Long userId) {
        return quizAttemptRepository.findByUserIdAndIsCompletedTrueAndScoreIsNotNullAndTotalQuestionsGreaterThanOrderByQuizIdAscEndTimeDesc(
                userId, 0)
            .stream()
            .map(this::createAttemptResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public QuizAttemptResponse getActiveAttempt(Long quizId, UserPrincipal userPrincipal) {
        Optional<QuizAttempt> activeAttempt = quizAttemptRepository.findActiveAttemptByQuizIdAndUserId(quizId, userPrincipal.getId());
        return activeAttempt.map(this::createAttemptResponse).orElse(null);
    }

    private QuizAttemptResponse createAttemptResponse(QuizAttempt attempt) {
        return QuizAttemptResponse.builder()
            .attemptId(attempt.getId())
            .quizId(attempt.getQuiz().getId())
            .quizTitle(attempt.getQuiz().getTitle())
            .score(attempt.getScore())
            .totalQuestions(attempt.getTotalQuestions())
            .timeSpent(attempt.getTimeSpent())
            .isCompleted(attempt.getIsCompleted())
            .endTime(attempt.getEndTime())
            .answers(attempt.getUserAnswers())
            .personalityResult(attempt.getPersonalityResult() != null ? 
                QuizResultDTO.fromEntity(attempt.getPersonalityResult()) : null)
            .build();
    }

    private QuizResult calculatePersonalityResult(Quiz quiz, Map<Integer, Integer> answers) {
        Map<String, Integer> traitScores = new HashMap<>();
        
        // Подсчитываем очки для каждой характеристики на основе ответов
        for (Map.Entry<Integer, Integer> entry : answers.entrySet()) {
            int questionIndex = entry.getKey();
            int selectedAnswer = entry.getValue();
            
            if (questionIndex < quiz.getQuestions().size()) {
                Question question = quiz.getQuestions().get(questionIndex);
                Option selectedOption = question.getOptions().get(selectedAnswer);
                
                // Добавляем очки характеристик из выбранного варианта
                Map<String, Integer> optionTraits = selectedOption.getPersonalityTraits();
                if (optionTraits != null) {
                    optionTraits.forEach((trait, score) -> 
                        traitScores.merge(trait, score, Integer::sum));
                }
            }
        }
        
        // Находим результат, который лучше всего соответствует набранным характеристикам
        return findBestMatchingResult(quiz.getResults(), traitScores);
    }

    private QuizResult findBestMatchingResult(List<QuizResult> results, Map<String, Integer> userTraits) {
        if (results == null || results.isEmpty()) {
            throw new IllegalStateException("Не настроены результаты для personality викторины");
        }
        
        QuizResult bestMatch = null;
        int bestMatchScore = Integer.MIN_VALUE;
        
        for (QuizResult result : results) {
            int matchScore = calculateMatchScore(result.getPersonalityTraits(), userTraits);
            if (matchScore > bestMatchScore) {
                bestMatchScore = matchScore;
                bestMatch = result;
            }
        }
        
        if (bestMatch == null) {
            throw new IllegalStateException("Не удалось определить результат personality викторины");
        }
        
        return bestMatch;
    }

    private int calculateMatchScore(Map<String, Integer> resultTraits, Map<String, Integer> userTraits) {
        if (resultTraits == null || userTraits == null) {
            return 0;
        }
        
        int score = 0;
        for (Map.Entry<String, Integer> trait : resultTraits.entrySet()) {
            Integer userScore = userTraits.get(trait.getKey());
            if (userScore != null) {
                // Чем меньше разница между ожидаемым и фактическим значением характеристики,
                // тем выше очки соответствия
                score += Math.max(0, 10 - Math.abs(trait.getValue() - userScore));
            }
        }
        return score;
    }
} 