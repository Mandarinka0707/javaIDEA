package ru.utalieva.victorina.model.dto.feed;

import lombok.Data;
import ru.utalieva.victorina.model.entity.FeedItem;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class FeedItemDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long quizId;
    private String quizTitle;
    private String quizType;
    private Integer score;
    private Integer totalQuestions;
    private Integer timeSpent;
    private Integer position;
    private String description;
    private String image;
    private LocalDateTime completedAt;
    private PersonalityResultDTO personalityResult;

    @Data
    public static class PersonalityResultDTO {
        private String title;
        private String description;
        private String image;
        private Map<String, Integer> traits;
    }

    public static FeedItemDTO fromEntity(FeedItem entity) {
        FeedItemDTO dto = new FeedItemDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser().getId());
        dto.setUsername(entity.getUser().getUsername());
        
        dto.setQuizId(entity.getQuiz() != null ? entity.getQuiz().getId() : null);
        dto.setQuizTitle(entity.getQuizTitle());
        dto.setQuizType(entity.getQuizType());
        dto.setScore(entity.getScore());
        dto.setTotalQuestions(entity.getTotalQuestions());
        dto.setTimeSpent(entity.getTimeSpent());
        dto.setPosition(entity.getPosition());
        dto.setDescription(entity.getDescription());
        dto.setImage(entity.getImage());
        dto.setCompletedAt(entity.getCompletedAt());
        
        if (entity.getPersonalityResult() != null) {
            PersonalityResultDTO personalityResult = new PersonalityResultDTO();
            personalityResult.setTitle(entity.getPersonalityResult().getTitle());
            personalityResult.setDescription(entity.getPersonalityResult().getDescription());
            personalityResult.setImage(entity.getPersonalityResult().getImage());
            // Note: traits mapping would go here if needed
            dto.setPersonalityResult(personalityResult);
        }
        
        return dto;
    }
} 