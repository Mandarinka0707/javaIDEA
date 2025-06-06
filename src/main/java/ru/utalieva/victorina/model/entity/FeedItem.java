package ru.utalieva.victorina.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.utalieva.victorina.model.enumination.PostType;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "feed_items")
@NoArgsConstructor
public class FeedItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(name = "quiz_title")
    private String quizTitle;

    @Column(name = "quiz_type")
    private String quizType;

    private Integer score;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "time_spent")
    private Integer timeSpent;

    private Integer position;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String image;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personality_result_id")
    private QuizResult personalityResult;

    @PrePersist
    protected void onCreate() {
        if (completedAt == null) {
            completedAt = LocalDateTime.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
} 