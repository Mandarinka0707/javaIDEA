package ru.utalieva.victorina.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class PersonalityQuestionDTO {
    private String question;    // Текст вопроса
    private String image;       // URL изображения (опционально)
    private List<PersonalityOptionDTO> options;  // Варианты ответов
} 