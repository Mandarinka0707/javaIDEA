package ru.utalieva.victorina.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.utalieva.victorina.model.dto.*;
import ru.utalieva.victorina.model.entity.Quiz;
import ru.utalieva.victorina.model.entity.User;
import ru.utalieva.victorina.model.enumination.QuizType;
import ru.utalieva.victorina.security.UserPrincipal;
import ru.utalieva.victorina.service.QuizService;
import ru.utalieva.victorina.service.UserService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/quizzes")
@CrossOrigin(origins = "http://localhost:3000")
public class QuizController {
    private static final Logger logger = LoggerFactory.getLogger(QuizController.class);
    private final QuizService quizService;
    private final UserService userService;

    public QuizController(QuizService quizService, UserService userService) {
        this.quizService = quizService;
        this.userService = userService;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createQuiz(@RequestBody QuizCreateDTO request, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User user = userService.findById(userPrincipal.getId());
            logger.info("Creating quiz for user: {}", user.getUsername());
            Quiz quiz = quizService.createQuiz(request, user.getUsername());
            return ResponseEntity.ok(QuizDTO.fromEntity(quiz));
        } catch (Exception e) {
            logger.error("Error creating quiz: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Ошибка при создании викторины: " + e.getMessage()));
        }
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllQuizzes() {
        try {
            logger.info("Getting all quizzes");
            List<QuizDTO> quizDTOs = quizService.getAllQuizzes();
            return ResponseEntity.ok(quizDTOs);
        } catch (Exception e) {
            logger.error("Error getting all quizzes: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Ошибка при получении викторин: " + e.getMessage()));
        }
    }

    @GetMapping("/my")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getMyQuizzes(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            if (userPrincipal == null) {
                logger.error("User not authenticated");
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Пользователь не аутентифицирован"));
            }
            
            User user = userService.findById(userPrincipal.getId());
            if (user == null) {
                logger.error("User not found for id: {}", userPrincipal.getId());
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Пользователь не найден"));
            }
            
            logger.info("Getting quizzes for user: {}", user.getUsername());
            List<QuizDTO> quizDTOs = quizService.getQuizzesByAuthor(user.getUsername());
            return ResponseEntity.ok(quizDTOs);
        } catch (Exception e) {
            logger.error("Error getting user quizzes: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Ошибка при получении викторин: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getQuizById(@PathVariable Long id) {
        try {
            logger.info("Getting quiz by id: {}", id);
            Quiz quiz = quizService.getQuizById(id);
            return ResponseEntity.ok(QuizDTO.fromEntity(quiz));
        } catch (Exception e) {
            logger.error("Error getting quiz by id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Ошибка при получении викторины: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteQuiz(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User user = userService.findById(userPrincipal.getId());
            logger.info("Deleting quiz {} by user: {}", id, user.getUsername());
            quizService.deleteQuiz(id, user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting quiz {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Ошибка при удалении викторины: " + e.getMessage()));
        }
    }

    @PostMapping("/personality")
    @Transactional
    public ResponseEntity<?> createPersonalityQuiz(
            @RequestBody PersonalityQuizCreateDTO request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User user = userService.findById(userPrincipal.getId());
            logger.info("Creating personality quiz for user: {}", user.getUsername());
            
            // Преобразуем PersonalityQuizCreateDTO в QuizCreateDTO
            QuizCreateDTO quizDTO = new QuizCreateDTO();
            quizDTO.setTitle(request.getTitle());
            quizDTO.setDescription(request.getDescription());
            quizDTO.setCategory(request.getCategory());
            quizDTO.setTimeDuration(request.getTimeDuration());
            quizDTO.setPublic(request.isPublic());
            quizDTO.setTags(request.getTags());
            quizDTO.setQuizType(QuizType.PERSONALITY);
            quizDTO.setDifficulty("EASY"); // Устанавливаем сложность по умолчанию для личностного теста
            
            // Преобразуем вопросы
            List<QuestionDTO> questions = request.getQuestions().stream()
                .map(pq -> {
                    QuestionDTO q = new QuestionDTO();
                    q.setQuestion(pq.getQuestion());
                    q.setImage(pq.getImage());
                    
                    // Преобразуем варианты ответов
                    List<OptionDTO> options = pq.getOptions().stream()
                        .map(po -> {
                            OptionDTO o = new OptionDTO();
                            o.setContent(po.getContent());
                            o.setType("TEXT"); // Для личностного теста всегда текст
                            
                            // Используем traits из PersonalityOptionDTO
                            Map<String, Integer> traits = new HashMap<>();
                            if (po.getTraits() != null && po.getTraits().get("resultIndex") != null) {
                                traits.put("resultIndex", po.getTraits().get("resultIndex"));
                            }
                            o.setTraits(traits);
                            
                            return o;
                        })
                        .collect(Collectors.toList());
                    q.setOptions(options);
                    
                    return q;
                })
                .collect(Collectors.toList());
            quizDTO.setQuestions(questions);
            
            // Преобразуем результаты
            List<Map<String, Object>> results = request.getResults().stream()
                .map(pr -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("title", pr.getTitle());
                    result.put("description", pr.getDescription());
                    result.put("image", pr.getImage());
                    return result;
                })
                .collect(Collectors.toList());
            quizDTO.setResults(results);

            Quiz quiz = quizService.createQuiz(quizDTO, user.getUsername());
            return ResponseEntity.ok(QuizDTO.fromEntity(quiz));
        } catch (Exception e) {
            logger.error("Error creating personality quiz: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Ошибка при создании викторины: " + e.getMessage()));
        }
    }
} 