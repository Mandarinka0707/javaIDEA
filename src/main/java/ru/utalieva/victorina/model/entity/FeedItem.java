package ru.utalieva.victorina.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "feed_items")
@Data
@NoArgsConstructor
public class FeedItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false)
    private String quizTitle;

    @Column(nullable = false)
    private String quizType;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    // Для обычных викторин
    private Integer score;
    private Integer totalQuestions;
    private Integer timeSpent;
    private Integer position;

    // Для личностных викторин
    private String character;
    private String description;
    private String image;

    @ElementCollection
    @CollectionTable(name = "feed_item_traits",
            joinColumns = @JoinColumn(name = "feed_item_id"))
    @MapKeyColumn(name = "trait_name")
    @Column(name = "trait_value")
    private Map<String, Integer> traits;
} 