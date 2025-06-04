package ru.utalieva.victorina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.utalieva.victorina.model.entity.Friendship;
import ru.utalieva.victorina.model.entity.FriendshipStatus;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    @Query("SELECT f FROM Friendship f WHERE (f.userId = :userId1 AND f.friendId = :userId2) OR (f.userId = :userId2 AND f.friendId = :userId1)")
    Optional<Friendship> findFriendshipByUserIds(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT f FROM Friendship f WHERE f.userId = :userId AND f.friendId = :friendId AND f.status = :status")
    Optional<Friendship> findByUserIdAndFriendIdAndStatus(
            @Param("userId") Long userId, 
            @Param("friendId") Long friendId, 
            @Param("status") FriendshipStatus status);

    List<Friendship> findByUserIdAndStatus(Long userId, FriendshipStatus status);
    List<Friendship> findByFriendIdAndStatus(Long friendId, FriendshipStatus status);
    void deleteByUserIdAndFriendId(Long userId, Long friendId);

    @Query("SELECT f FROM Friendship f WHERE (f.userId = :userId OR f.friendId = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findAllAcceptedFriendships(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendship f " +
           "WHERE ((f.userId = :userId1 AND f.friendId = :userId2) OR " +
           "(f.userId = :userId2 AND f.friendId = :userId1)) AND f.status = 'ACCEPTED'")
    boolean existsAcceptedFriendship(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("""
        SELECT COUNT(f) FROM Friendship f 
        WHERE (f.userId = :userId OR f.friendId = :userId) 
        AND f.status = 'ACCEPTED'
        """)
    int countAcceptedFriendships(@Param("userId") Long userId);
} 