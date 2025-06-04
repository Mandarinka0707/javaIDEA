package ru.utalieva.victorina.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.utalieva.victorina.model.dto.quiz.QuizCreateDTO;
import ru.utalieva.victorina.model.dto.quiz.QuizDTO;
import ru.utalieva.victorina.model.dto.quiz.QuestionDTO;
import ru.utalieva.victorina.model.entity.Quiz;
import ru.utalieva.victorina.model.entity.Question;
import ru.utalieva.victorina.model.entity.Option;
import ru.utalieva.victorina.model.entity.QuizResult;
import ru.utalieva.victorina.model.entity.User;
import ru.utalieva.victorina.model.enumination.QuizType;
import ru.utalieva.victorina.repository.QuizRepository;
import ru.utalieva.victorina.repository.QuizResultRepository;
import ru.utalieva.victorina.repository.UserRepository;

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

    @Transactional
    public Quiz createQuiz(QuizCreateDTO quizDTO, String username) {
        try {
            logger.debug("Starting quiz creation for user: {}", username);
            
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
            
            // Сохраняем теги
            if (quizDTO.getTags() != null && !quizDTO.getTags().isEmpty()) {
                quiz.setTags(new ArrayList<>(quizDTO.getTags()));
                logger.debug("Added {} tags to quiz", quizDTO.getTags().size());
            }
            
            // Validate questions
            if (quizDTO.getQuestions() == null || quizDTO.getQuestions().isEmpty()) {
                throw new IllegalArgumentException("Викторина должна содержать хотя бы один вопрос");
            }

            // Конвертируем QuestionDTO в Question
            List<Question> questions = quizDTO.getQuestions().stream()
                    .map(questionDTO -> convertToQuestion(questionDTO, quiz))
                    .collect(Collectors.toList());
            quiz.setQuestions(questions);

            Quiz savedQuiz = quizRepository.save(quiz);
            logger.debug("Quiz saved successfully with ID: {}", savedQuiz.getId());

            // Создаем результаты в зависимости от типа викторины
            if (QuizType.PERSONALITY.equals(quiz.getQuizType())) {
                if (quizDTO.getResults() == null || quizDTO.getResults().isEmpty()) {
                    throw new IllegalArgumentException("Для теста личности необходимо добавить варианты результатов");
                }
                createPersonalityResults(savedQuiz, quizDTO.getResults());
            } else {
                createStandardResults(savedQuiz, quizDTO.getQuestions().size());
            }

            logger.debug("Quiz creation completed successfully");
            return savedQuiz;
            
        } catch (Exception e) {
            logger.error("Error creating quiz: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при создании викторины: " + e.getMessage());
        }
    }

    private Question convertToQuestion(QuestionDTO questionDTO, Quiz quiz) {
        try {
            if (questionDTO.getOptions() == null || questionDTO.getOptions().isEmpty()) {
                throw new IllegalArgumentException("Вопрос должен содержать варианты ответов");
            }

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
                        option.setImage(optionDTO.getImage());
                        option.setTraits(optionDTO.getTraits());
                        return option;
                    })
                    .collect(Collectors.toList());
            question.setOptions(options);
            
            // Validate correctIndex
            if (quiz.getQuizType() == QuizType.STANDARD) {
                if (questionDTO.getCorrectIndex() == null) {
                    throw new IllegalArgumentException("Для стандартной викторины необходимо указать правильный ответ");
                }
                if (questionDTO.getCorrectIndex() < 0 || questionDTO.getCorrectIndex() >= options.size()) {
                    throw new IllegalArgumentException("Индекс правильного ответа некорректен");
                }
            }
            question.setCorrectIndex(questionDTO.getCorrectIndex());
            
            return question;
        } catch (Exception e) {
            logger.error("Error converting question DTO to entity", e);
            throw e;
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