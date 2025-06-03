package ru.utalieva.victorina.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.utalieva.victorina.model.dto.QuizAttemptRequest;
import ru.utalieva.victorina.model.dto.QuizAttemptResponse;
import ru.utalieva.victorina.model.dto.QuizResultDTO;
import ru.utalieva.victorina.model.entity.Quiz;
import ru.utalieva.victorina.model.entity.QuizAttempt;
import ru.utalieva.victorina.model.entity.User;
import ru.utalieva.victorina.model.entity.Question;
import ru.utalieva.victorina.model.entity.Option;
import ru.utalieva.victorina.model.entity.QuizResult;
import ru.utalieva.victorina.model.enumination.QuizType;
import ru.utalieva.victorina.repository.QuizAttemptRepository;
import ru.utalieva.victorina.repository.QuizRepository;
import ru.utalieva.victorina.repository.UserRepository;
import ru.utalieva.victorina.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizAttemptService {
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(QuizAttemptService.class);

    @Transactional
    public QuizAttemptResponse startQuiz(Long userId, Long quizId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setUser(user);
        attempt.setStartTime(LocalDateTime.now());
        attempt.setScore(0);
        attempt.setTotalQuestions(quiz.getQuestions().size());
        attempt.setTimeSpent(0);
        attempt.setIsCompleted(false);
        attempt.setUserAnswers(new HashMap<>());

        attempt = quizAttemptRepository.save(attempt);
        return createAttemptResponse(attempt);
    }

    @Transactional
    public QuizAttemptResponse submitQuiz(Long userId, QuizAttemptRequest request) {
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        List<QuizAttempt> attempts = quizAttemptRepository.findByUserIdAndQuizId(userId, request.getQuizId());
        QuizAttempt attempt = attempts.stream()
                .filter(a -> !a.getIsCompleted())
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Active attempt not found"));

        // Convert Integer keys to String for JSONB compatibility
        Map<String, Integer> userAnswers = request.getAnswers().entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> String.valueOf(entry.getKey()),
                    Map.Entry::getValue
                ));

        attempt.setUserAnswers(userAnswers);
        attempt.setTimeSpent(request.getTimeSpent());
        attempt.setEndTime(LocalDateTime.now());
        attempt.setIsCompleted(true);

        if (QuizType.PERSONALITY.equals(quiz.getQuizType())) {
            // Для личностной викторины вычисляем черты характера
            Map<String, Integer> traits = calculatePersonalityTraits(quiz, request.getAnswers());
            QuizResult personalityResult = findBestMatchingResult(quiz, traits);
            
            if (personalityResult != null) {
                attempt.setPersonalityResult(personalityResult);
                attempt.setScore(attempt.getTotalQuestions()); // Для personality quiz всегда максимальный счет
            } else {
                logger.warn("Could not determine personality result for quiz attempt: {}", attempt.getId());
                // Используем первый результат как запасной вариант
                attempt.setPersonalityResult(quiz.getResults().isEmpty() ? null : quiz.getResults().getFirst());
            }
        } else {
            // Для стандартной викторины считаем очки
            int score = calculateScore(quiz, request.getAnswers());
            attempt.setScore(score);
        }

        attempt = quizAttemptRepository.save(attempt);
        return createAttemptResponse(attempt);
    }

    private int calculateScore(Quiz quiz, Map<Integer, Integer> answers) {
        int score = 0;
        List<Question> questions = quiz.getQuestions();
        
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            List<Option> options = question.getOptions();
            
            Integer selectedOption = answers.get(i);
            if (selectedOption != null && selectedOption >= 0 && selectedOption < options.size()) {
                if (selectedOption.equals(question.getCorrectIndex())) {
                    score++;
                }
            }
        }
        return score;
    }

    private Map<String, Integer> calculatePersonalityTraits(Quiz quiz, Map<Integer, Integer> answers) {
        Map<String, Integer> traits = new HashMap<>();
        List<Question> questions = quiz.getQuestions();
        
        for (Map.Entry<Integer, Integer> answer : answers.entrySet()) {
            int questionIndex = answer.getKey();
            int selectedOptionIndex = answer.getValue();
            
            if (questionIndex >= 0 && questionIndex < questions.size()) {
                Question question = questions.get(questionIndex);
                List<Option> options = question.getOptions();
                
                if (selectedOptionIndex >= 0 && selectedOptionIndex < options.size()) {
                    Option selectedOption = options.get(selectedOptionIndex);
                    Map<String, Integer> optionTraits = selectedOption.getTraits();
                    
                    if (optionTraits != null) {
                        // Store the resultIndex for this specific answer
                        traits.put("resultIndex_" + questionIndex, optionTraits.get("resultIndex"));
                        
                        // Also store the most recent resultIndex as the main one
                        traits.put("resultIndex", optionTraits.get("resultIndex"));
                    }
                }
            }
        }
        
        return traits;
    }

    private QuizResult findBestMatchingResult(Quiz quiz, Map<String, Integer> userTraits) {
        // For personality quizzes, we use the resultIndex from the traits
        Integer resultIndex = userTraits.get("resultIndex");
        if (resultIndex != null && resultIndex >= 0 && resultIndex < quiz.getResults().size()) {
            return quiz.getResults().get(resultIndex);
        }
        
        // Fallback: find the most common resultIndex in user's answers
        Map<Integer, Integer> resultCounts = new HashMap<>();
        userTraits.forEach((trait, value) -> {
            if (trait.startsWith("resultIndex_")) {
                resultCounts.merge(value, 1, Integer::sum);
            }
        });
        
        if (!resultCounts.isEmpty()) {
            // Find the most frequent resultIndex
            return resultCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> {
                    int index = entry.getKey();
                    return index >= 0 && index < quiz.getResults().size() ? 
                        quiz.getResults().get(index) : null;
                })
                .orElse(null);
        }
        
        // If no result can be determined, return the first result as default
        return quiz.getResults().isEmpty() ? null : quiz.getResults().getFirst();
    }

    private QuizAttemptResponse createAttemptResponse(QuizAttempt attempt) {
        QuizAttemptResponse response = new QuizAttemptResponse();
        response.setAttemptId(attempt.getId());
        response.setQuizId(attempt.getQuiz().getId());
        response.setQuizTitle(attempt.getQuiz().getTitle());
        response.setTotalQuestions(attempt.getTotalQuestions());
        response.setTimeSpent(attempt.getTimeSpent());
        response.setIsCompleted(attempt.getIsCompleted());
        
        if (attempt.getIsCompleted() && attempt.getUserAnswers() != null) {
            if (QuizType.PERSONALITY.equals(attempt.getQuiz().getQuizType())) {
                // Для викторины типа "Кто ты"
                if (attempt.getPersonalityResult() != null) {
                    response.setPersonalityResult(QuizResultDTO.fromEntity(attempt.getPersonalityResult()));
                    response.setScore(attempt.getTotalQuestions()); // Для personality quiz всегда показываем максимальный счет
                } else {
                    logger.warn("No personality result found for completed personality quiz attempt: {}", attempt.getId());
                    // Используем первый результат как запасной вариант
                    if (!attempt.getQuiz().getResults().isEmpty()) {
                        response.setPersonalityResult(QuizResultDTO.fromEntity(attempt.getQuiz().getResults().getFirst()));
                        response.setScore(attempt.getTotalQuestions());
                    }
                }
            } else {
                // Для стандартной викторины
                response.setScore(attempt.getScore());
                // Конвертируем ответы из Map<String, Integer> в Map<Integer, Boolean>
                Map<Integer, Boolean> answers = new HashMap<>();
                attempt.getUserAnswers().forEach((key, value) -> {
                    int questionIndex = Integer.parseInt(key);
                    Question question = attempt.getQuiz().getQuestions().get(questionIndex);
                    answers.put(questionIndex, value.equals(question.getCorrectIndex()));
                });
                response.setAnswers(answers);
            }
        }
        
        return response;
    }

    public List<QuizAttemptResponse> getUserAttempts(Long userId) {
        return quizAttemptRepository.findCompletedAttemptsByUserId(userId).stream()
                .map(this::createAttemptResponse)
                .toList();
    }
} 