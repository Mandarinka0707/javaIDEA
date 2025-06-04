package ru.utalieva.victorina.model.dto.quiz;

import lombok.Data;
import ru.utalieva.victorina.model.enumination.QuizType;
import java.util.List;
import java.util.Map;

@Data
public class QuizCreateDTO {
    private String title;
    private String description;
    private String category;
    private String difficulty;
    private QuizType quizType;
    private Integer timeDuration;
    private boolean isPublic;
    private List<String> tags;
    private List<QuestionDTO> questions;
    private List<Map<String, Object>> results;
} 