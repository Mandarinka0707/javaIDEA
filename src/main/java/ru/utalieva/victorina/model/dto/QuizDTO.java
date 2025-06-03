package ru.utalieva.victorina.model.dto;

import lombok.Data;
import ru.utalieva.victorina.model.entity.Question;
import ru.utalieva.victorina.model.entity.QuizResult;
import ru.utalieva.victorina.model.entity.Quiz;
import ru.utalieva.victorina.model.enumination.QuizType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class QuizDTO {
    private Long id;
    private String title;
    private String description;
    private String category;
    private QuizType quizType;
    private String difficulty;
    private Integer timeDuration;
    private List<String> tags;
    private boolean isPublic;
    private Long authorId;
    private String authorUsername;
    private LocalDateTime createdAt;
    private List<QuestionDTO> questions;
    private List<QuizResultDTO> results;

    public static QuizDTO fromEntity(Quiz quiz) {
        QuizDTO dto = new QuizDTO();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setCategory(quiz.getCategory());
        dto.setQuizType(quiz.getQuizType());
        dto.setDifficulty(quiz.getDifficulty());
        dto.setTimeDuration(quiz.getTimeDuration());
        dto.setTags(quiz.getTags());
        dto.setPublic(quiz.isPublic());
        dto.setAuthorId(quiz.getAuthor().getId());
        dto.setAuthorUsername(quiz.getAuthor().getUsername());
        dto.setCreatedAt(quiz.getCreatedAt());
        
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