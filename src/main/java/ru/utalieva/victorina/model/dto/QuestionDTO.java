package ru.utalieva.victorina.model.dto;

import lombok.Data;
import ru.utalieva.victorina.model.entity.Question;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class QuestionDTO {
    private String question;
    private String image;
    private List<OptionDTO> options;
    private Integer correctIndex;

    public static QuestionDTO fromEntity(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setQuestion(question.getQuestion());
        dto.setImage(question.getImage());
        dto.setCorrectIndex(question.getCorrectIndex());
        
        if (question.getOptions() != null) {
            dto.setOptions(question.getOptions().stream()
                .map(OptionDTO::fromEntity)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
} 