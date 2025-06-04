package ru.utalieva.victorina.model.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class FeedItemDTO {
    private Long id;
    private Long quizId;
    private String quizTitle;
    private String quizType;
    private LocalDateTime completedAt;
    
    // Для обычных викторин
    private Integer score;
    private Integer totalQuestions;
    private Integer timeSpent;
    private Integer position;
    
    // Для личностных викторин
    private String character;
    private String description;
    private String image;
    private Map<String, Integer> traits;
} 