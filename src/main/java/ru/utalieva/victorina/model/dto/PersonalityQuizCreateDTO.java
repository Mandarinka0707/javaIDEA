package ru.utalieva.victorina.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class PersonalityQuizCreateDTO {
    private String title;           // Название викторины
    private String description;     // Описание викторины
    private String category;        // Категория (например, "Аниме", "Наруто")
    private Integer timeDuration;   // Длительность в минутах
    private boolean isPublic;       // Публичная ли викторина
    private List<String> tags;      // Теги для поиска

    private List<PersonalityTraitDTO> traits;        // Доступные характеристики
    private List<PersonalityQuestionDTO> questions;   // Вопросы
    private List<PersonalityResultDTO> results;       // Возможные результаты (персонажи)
} 