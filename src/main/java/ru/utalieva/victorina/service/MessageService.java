package ru.utalieva.victorina.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.utalieva.victorina.model.entity.Message;
import ru.utalieva.victorina.model.entity.User;
import ru.utalieva.victorina.repository.MessageRepository;
import ru.utalieva.victorina.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FriendshipService friendshipService;

    public Message sendMessage(User sender, Long receiverId, String content) {
        logger.info("Checking friendship status between users {} and {}", sender.getId(), receiverId);
        if (!friendshipService.areFriends(sender.getId(), receiverId)) {
            logger.error("Users {} and {} are not friends", sender.getId(), receiverId);
            throw new IllegalStateException("Can only send messages to friends");
        }

        logger.info("Creating new message from user {} to user {}", sender.getId(), receiverId);
        Message message = new Message();
        message.setSenderId(sender.getId());
        message.setReceiverId(receiverId);
        message.setContent(content);
        message = messageRepository.save(message);
        logger.info("Message {} saved successfully", message.getId());
        return message;
    }

    public List<Message> getChatMessages(Long userId1, Long userId2) {
        logger.info("Checking friendship status between users {} and {}", userId1, userId2);
        if (!friendshipService.areFriends(userId1, userId2)) {
            logger.error("Users {} and {} are not friends", userId1, userId2);
            throw new IllegalStateException("Can only view messages with friends");
        }

        logger.info("Fetching chat messages between users {} and {}", userId1, userId2);
        List<Message> messages = messageRepository.findChatMessages(userId1, userId2);
        logger.info("Found {} messages between users {} and {}", messages.size(), userId1, userId2);
        return messages;
    }

    public void markAsRead(Long messageId, User user) {
        logger.info("Finding message {} to mark as read", messageId);
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> {
                    logger.error("Message {} not found", messageId);
                    return new IllegalArgumentException("Message not found");
                });

        if (!message.getReceiverId().equals(user.getId())) {
            logger.error("User {} attempted to mark message {} as read but is not the receiver", user.getId(), messageId);
            throw new IllegalStateException("Can only mark your own received messages as read");
        }

        logger.info("Marking message {} as read", messageId);
        message.setReadAt(LocalDateTime.now());
        messageRepository.save(message);
        logger.info("Message {} marked as read successfully", messageId);
    }

    public List<Message> getUnreadMessages(User user) {
        logger.info("Getting unread messages for user {}", user.getId());
        List<Message> messages = messageRepository.findUnreadMessages(user.getId());
        logger.info("Found {} unread messages for user {}", messages.size(), user.getId());
        return messages;
    }

    public List<Map<String, Object>> getUserChats(User user) {
        logger.info("Getting chat partners for user {}", user.getId());
        List<Long> chatPartnerIds = messageRepository.findUserChats(user.getId());
        logger.info("Found {} chat partners for user {}", chatPartnerIds.size(), user.getId());
        
        return chatPartnerIds.stream()
            .map(partnerId -> {
                User partner = userRepository.findById(partnerId).orElse(null);
                if (partner == null) {
                    logger.warn("Chat partner {} not found", partnerId);
                    return null;
                }

                List<Message> messages = messageRepository.findChatMessages(user.getId(), partnerId);
                Message lastMessage = messages.isEmpty() ? null : messages.get(0);
                
                Map<String, Object> chatInfo = new HashMap<>();
                chatInfo.put("partnerId", partnerId);
                chatInfo.put("partnerName", partner.getUsername());
                chatInfo.put("lastMessage", lastMessage != null ? lastMessage.getContent() : "");
                chatInfo.put("lastMessageTime", lastMessage != null ? lastMessage.getSentAt() : null);
                chatInfo.put("unreadCount", messages.stream()
                    .filter(m -> m.getReceiverId().equals(user.getId()) && m.getReadAt() == null)
                    .count());
                
                return chatInfo;
            })
            .filter(chat -> chat != null)
            .collect(Collectors.toList());
    }
} 