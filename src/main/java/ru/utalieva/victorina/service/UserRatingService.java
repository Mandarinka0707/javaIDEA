package ru.utalieva.victorina.service;

import ru.utalieva.victorina.model.entity.UserRating;
import java.util.List;

public interface UserRatingService {
    void updateUserRating(Long userId);
    List<UserRating> getTopUsers(int limit);
    Integer getUserRank(Long userId);
    UserRating getUserRating(Long userId);
    void recalculateUserRating(Long userId);
} 