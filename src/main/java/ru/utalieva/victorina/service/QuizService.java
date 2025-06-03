package ru.utalieva.victorina.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.utalieva.victorina.model.dto.QuizCreateDTO;
import ru.utalieva.victorina.model.dto.QuizDTO;
import ru.utalieva.victorina.model.dto.QuestionDTO;
import ru.utalieva.victorina.model.dto.OptionDTO;
import ru.utalieva.victorina.model.entity.*;
import ru.utalieva.victorina.model.enumination.QuizType;
import ru.utalieva.victorina.repository.QuizRepository;
import ru.utalieva.victorina.repository.QuizResultRepository;
import ru.utalieva.victorina.repository.UserRepository;
import ru.utalieva.victorina.security.SecurityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class QuizService {
    private static final Logger logger = LoggerFactory.getLogger(QuizService.class);
    private final QuizRepository quizRepository;
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final ObjectMapper objectMapper;

    @Transactional
    public Quiz createQuiz(QuizCreateDTO quizDTO, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Quiz quiz = new Quiz();
        quiz.setTitle(quizDTO.getTitle());
        quiz.setDescription(quizDTO.getDescription());
        quiz.setCategory(quizDTO.getCategory());
        quiz.setDifficulty(quizDTO.getDifficulty());
        quiz.setQuizType(quizDTO.getQuizType() != null ? quizDTO.getQuizType() : QuizType.STANDARD);
        quiz.setTimeDuration(quizDTO.getTimeDuration());
        quiz.setPublic(quizDTO.isPublic());
        quiz.setAuthor(user);
        
        // Конвертируем QuestionDTO в Question
        List<Question> questions = quizDTO.getQuestions().stream()
                .map(questionDTO -> convertToQuestion(questionDTO, quiz))
                .collect(Collectors.toList());
        quiz.setQuestions(questions);

        Quiz savedQuiz = quizRepository.save(quiz);

        // Создаем результаты в зависимости от типа викторины
        if (QuizType.PERSONALITY.equals(quiz.getQuizType())) {
            createPersonalityResults(savedQuiz, quizDTO.getResults());
        } else {
            createStandardResults(savedQuiz, quizDTO.getQuestions().size());
        }

        return savedQuiz;
    }

    private Question convertToQuestion(QuestionDTO questionDTO, Quiz quiz) {
        try {
            Question question = new Question();
            question.setQuiz(quiz);
            question.setQuestion(questionDTO.getQuestion());
            question.setImage(questionDTO.getImage());
            
            List<Option> options = questionDTO.getOptions().stream()
                    .map(optionDTO -> {
                        Option option = new Option();
                        option.setQuestion(question);
                        option.setContent(optionDTO.getContent());
                        option.setType(optionDTO.getType());
                        option.setTraits(optionDTO.getTraits());
                        return option;
                    })
                    .collect(Collectors.toList());
            question.setOptions(options);
            question.setCorrectIndex(questionDTO.getCorrectIndex());
            
            return question;
        } catch (Exception e) {
            logger.error("Error converting question: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при создании вопроса: " + e.getMessage());
        }
    }

    private void createPersonalityResults(Quiz quiz, List<Map<String, Object>> results) {
        if (results == null || results.isEmpty()) {
            throw new RuntimeException("Для теста личности необходимо добавить варианты результатов");
        }

        results.forEach(resultData -> {
            QuizResult result = new QuizResult();
            result.setQuiz(quiz);
            result.setTitle((String) resultData.get("title"));
            result.setDescription((String) resultData.get("description"));
            result.setImage((String) resultData.get("image"));
            
            // Initialize empty maps for traits and target answers
            result.setPersonalityTraits(new HashMap<>());
            result.setTargetAnswers(new HashMap<>());

            quizResultRepository.save(result);
        });
    }

    private void createStandardResults(Quiz quiz, int questionCount) {
        // Создаем диапазоны результатов для стандартной викторины
        int[] ranges = {0, 40, 60, 80, 100};
        String[] descriptions = {
            "Попробуйте еще раз! У вас все получится!",
            "Неплохой результат! Есть куда расти.",
            "Хороший результат! Вы молодец!",
            "Отличный результат! Вы настоящий эксперт!"
        };

        for (int i = 0; i < descriptions.length; i++) {
            QuizResult result = new QuizResult();
            result.setQuiz(quiz);
            result.setTitle("Результат " + (i + 1));
            result.setDescription(descriptions[i]);
            result.setMinScore(Math.round(ranges[i] * questionCount / 100f));
            result.setMaxScore(Math.round(ranges[i + 1] * questionCount / 100f));
            
            // Для стандартной викторины эти поля не используются
            result.setPersonalityTraits(new HashMap<>());
            result.setTargetAnswers(new HashMap<>());
            
            quizResultRepository.save(result);
        }
    }

    @Transactional(readOnly = true)
    public List<QuizDTO> getAllQuizzes() {
        return quizRepository.findAll().stream()
                .map(QuizDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuizDTO> getQuizzesByAuthor(String username) {
        return quizRepository.findByAuthorUsername(username).stream()
                .map(QuizDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isQuizOwner(Quiz quiz, User user) {
        return quiz.getAuthor().getId().equals(user.getId());
    }

    @Transactional(readOnly = true)
    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Викторина не найдена"));
    }

    @Transactional
    public void deleteQuiz(Long id, User user) {
        Quiz quiz = getQuizById(id);
        if (!quiz.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("User is not authorized to delete this quiz");
        }
        quizRepository.delete(quiz);
        logger.info("Deleted quiz with id: {}", id);
    }
} 