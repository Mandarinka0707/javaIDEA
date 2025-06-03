package ru.utalieva.victorina.model.dto;

import lombok.Data;
import ru.utalieva.victorina.model.entity.QuizResult;
import java.util.Map;

@Data
public class QuizResultDTO {
    private Long id;
    private String title;
    private String description;
    private String image;
    private Integer minScore;
    private Integer maxScore;
    private Map<String, Integer> personalityTraits;
    private Map<String, Integer> targetAnswers;

    public static QuizResultDTO fromEntity(QuizResult result) {
        QuizResultDTO dto = new QuizResultDTO();
        dto.setId(result.getId());
        dto.setTitle(result.getTitle());
        dto.setDescription(result.getDescription());
        dto.setImage(result.getImage());
        dto.setMinScore(result.getMinScore());
        dto.setMaxScore(result.getMaxScore());
        dto.setPersonalityTraits(result.getPersonalityTraits());
        dto.setTargetAnswers(result.getTargetAnswers());
        return dto;
    }
} 