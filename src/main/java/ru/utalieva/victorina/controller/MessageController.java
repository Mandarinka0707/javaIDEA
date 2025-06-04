package ru.utalieva.victorina.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.utalieva.victorina.model.entity.Message;
import ru.utalieva.victorina.model.entity.User;
import ru.utalieva.victorina.security.UserPrincipal;
import ru.utalieva.victorina.service.MessageService;
import ru.utalieva.victorina.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class MessageController {
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
    private final MessageService messageService;
    private final UserService userService;

    @PostMapping("/send/{receiverId}")
    public ResponseEntity<Message> sendMessage(
            @PathVariable Long receiverId,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        try {
            logger.info("Attempting to send message to user {}", receiverId);
            User sender = userService.findById(userPrincipal.getId());
            Message message = messageService.sendMessage(sender, receiverId, payload.get("content"));
            logger.info("Message sent successfully to user {}", receiverId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            logger.error("Error sending message to user {}: {}", receiverId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/chat/{userId}")
    public ResponseEntity<List<Message>> getChatMessages(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        try {
            logger.info("Getting chat messages between users {} and {}", userPrincipal.getId(), userId);
            List<Message> messages = messageService.getChatMessages(userPrincipal.getId(), userId);
            logger.info("Found {} messages in chat between users {} and {}", 
                messages.size(), userPrincipal.getId(), userId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            logger.error("Error getting chat messages between users {} and {}: {}", 
                userPrincipal.getId(), userId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/{messageId}/read")
    public ResponseEntity<?> markMessageAsRead(
            @PathVariable Long messageId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        try {
            logger.info("Marking message {} as read for user {}", messageId, userPrincipal.getId());
            User user = userService.findById(userPrincipal.getId());
            messageService.markAsRead(messageId, user);
            logger.info("Message {} marked as read successfully", messageId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error marking message {} as read: {}", messageId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Message>> getUnreadMessages(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        try {
            logger.info("Getting unread messages for user {}", userPrincipal.getId());
            User user = userService.findById(userPrincipal.getId());
            List<Message> unreadMessages = messageService.getUnreadMessages(user);
            logger.info("Found {} unread messages for user {}", unreadMessages.size(), userPrincipal.getId());
            return ResponseEntity.ok(unreadMessages);
        } catch (Exception e) {
            logger.error("Error getting unread messages for user {}: {}", 
                userPrincipal.getId(), e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/chats")
    public ResponseEntity<List<Map<String, Object>>> getUserChats(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        try {
            logger.info("Getting chats for user {}", userPrincipal.getId());
            User user = userService.findById(userPrincipal.getId());
            List<Map<String, Object>> chats = messageService.getUserChats(user);
            logger.info("Found {} chats for user {}", chats.size(), userPrincipal.getId());
            return ResponseEntity.ok(chats);
        } catch (Exception e) {
            logger.error("Error getting chats for user {}: {}", userPrincipal.getId(), e.getMessage(), e);
            throw e;
        }
    }
} 