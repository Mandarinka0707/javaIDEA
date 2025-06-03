package ru.utalieva.victorina.model.enumination;

public enum QuizType {
    STANDARD("Стандартная викторина"),
    PERSONALITY("Тест на определение персонажа");

    private final String displayName;

    QuizType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 