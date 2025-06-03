package ru.utalieva.victorina.model.dto;

import lombok.Data;
import java.util.Map;

@Data
public class PersonalityOptionDTO {
    private String content;          // Текст варианта ответа
    private Map<String, Integer> traits;  // Влияние на характеристики
} 