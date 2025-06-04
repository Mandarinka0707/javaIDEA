package ru.utalieva.victorina.model.dto.personality;

import lombok.Data;
import java.util.Map;

@Data
public class PersonalityResultDTO {
    private String title;           // Имя персонажа
    private String description;     // Описание персонажа
    private String image;           // URL изображения персонажа
    private Map<String, Integer> traits;  // Характеристики персонажа
} 