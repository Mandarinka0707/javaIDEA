package ru.utalieva.victorina.model.enumination;

import lombok.Getter;

@Getter
public enum QuizType {
    STANDARD("Стандартная"),
    PERSONALITY("Личностный тест");

    private final String displayName;

    QuizType(String displayName) {
        this.displayName = displayName;
    }
} 