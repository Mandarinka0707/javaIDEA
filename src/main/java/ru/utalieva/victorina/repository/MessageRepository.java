package ru.utalieva.victorina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.utalieva.victorina.model.entity.Message;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE (m.senderId = :userId1 AND m.receiverId = :userId2) OR (m.senderId = :userId2 AND m.receiverId = :userId1) ORDER BY m.sentAt DESC")
    List<Message> findChatMessages(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT m FROM Message m WHERE m.receiverId = :userId AND m.readAt IS NULL ORDER BY m.sentAt DESC")
    List<Message> findUnreadMessages(@Param("userId") Long userId);

    @Query("""
        SELECT DISTINCT 
            CASE 
                WHEN m.senderId = :userId THEN m.receiverId 
                ELSE m.senderId 
            END as chatPartnerId 
        FROM Message m 
        WHERE m.senderId = :userId OR m.receiverId = :userId 
        ORDER BY MAX(m.sentAt) DESC
    """)
    List<Long> findUserChats(@Param("userId") Long userId);
} 