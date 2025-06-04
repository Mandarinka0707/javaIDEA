package ru.utalieva.victorina.model.dto.quiz;

import lombok.Data;
import ru.utalieva.victorina.model.dto.attempt.QuizResultDTO;
import ru.utalieva.victorina.model.entity.Quiz;
import ru.utalieva.victorina.model.enumination.QuizType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class QuizDTO {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String difficulty;
    private QuizType quizType;
    private Integer timeDuration;
    private boolean isPublic;
    private List<String> tags;
    private List<QuestionDTO> questions;
    private String authorUsername;
    private LocalDateTime createdAt;
    private BigDecimal rating;
    private Integer ratingCount;
    private List<QuizResultDTO> results;

    public static QuizDTO fromEntity(Quiz quiz) {
        QuizDTO dto = new QuizDTO();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setCategory(quiz.getCategory());
        dto.setDifficulty(quiz.getDifficulty());
        dto.setQuizType(quiz.getQuizType());
        dto.setTimeDuration(quiz.getTimeDuration());
        dto.setPublic(quiz.isPublic());
        dto.setTags(quiz.getTags());
        dto.setAuthorUsername(quiz.getAuthor() != null ? quiz.getAuthor().getUsername() : null);
        dto.setCreatedAt(quiz.getCreatedAt());
        dto.setRating(quiz.getAverageRating());
        dto.setRatingCount(quiz.getRatingCount());
        
        if (quiz.getQuestions() != null) {
            dto.setQuestions(quiz.getQuestions().stream()
                .map(QuestionDTO::fromEntity)
                .collect(Collectors.toList()));
        }
        
        if (quiz.getResults() != null) {
            dto.setResults(quiz.getResults().stream()
                .map(QuizResultDTO::fromEntity)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
} 