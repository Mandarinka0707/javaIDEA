package ru.utalieva.victorina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.utalieva.victorina.model.entity.UserRating;
import java.util.List;
import java.util.Optional;

public interface UserRatingRepository extends JpaRepository<UserRating, Long> {
    Optional<UserRating> findByUserId(Long userId);

    @Query("SELECT r FROM UserRating r ORDER BY r.averageScore DESC")
    List<UserRating> findAllOrderByAverageScoreDesc();

    @Query(value = """
        SELECT COUNT(*) + 1 FROM user_ratings ur 
        WHERE ur.average_score > (
            SELECT average_score FROM user_ratings WHERE user_id = :userId
        )
        """, 
        nativeQuery = true)
    Integer getUserRank(@Param("userId") Long userId);
} 