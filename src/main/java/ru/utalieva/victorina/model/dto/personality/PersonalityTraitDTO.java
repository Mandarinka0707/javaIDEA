package ru.utalieva.victorina.model.dto.personality;

import lombok.Data;

@Data
public class PersonalityTraitDTO {
    private String name;        // Название характеристики (например, "смелость")
    private String description; // Описание характеристики
    private Integer minValue;   // Минимальное значение (обычно 0)
    private Integer maxValue;   // Максимальное значение (обычно 5)
} 