package ru.utalieva.victorina.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.utalieva.victorina.model.entity.Friendship;
import ru.utalieva.victorina.model.entity.User;
import ru.utalieva.victorina.model.entity.FriendshipStatus;
import ru.utalieva.victorina.repository.FriendshipRepository;
import ru.utalieva.victorina.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendshipService {
    private static final Logger logger = LoggerFactory.getLogger(FriendshipService.class);
    
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public void sendFriendRequest(User user, Long friendId) {
        if (user.getId().equals(friendId)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + friendId));

        if (areFriends(user.getId(), friendId)) {
            throw new IllegalStateException("Already friends");
        }

        Optional<Friendship> existingRequest = friendshipRepository.findFriendshipByUserIds(user.getId(), friendId);
        if (existingRequest.isPresent()) {
            Friendship friendship = existingRequest.get();
            if (friendship.getStatus() == FriendshipStatus.PENDING) {
                throw new IllegalStateException("Friend request already exists");
            }
        }

        Friendship friendship = new Friendship();
        friendship.setUserId(user.getId());
        friendship.setFriendId(friendId);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendshipRepository.save(friendship);
    }

    public void acceptFriendRequest(User user, Long friendId) {
        Friendship friendship = friendshipRepository.findByUserIdAndFriendIdAndStatus(friendId, user.getId(), FriendshipStatus.PENDING)
                .orElseThrow(() -> new IllegalStateException("Friend request not found"));
        
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);
    }

    public void rejectFriendRequest(User user, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Friendship request not found"));

        if (!friendship.getFriendId().equals(user.getId())) {
            throw new IllegalArgumentException("Not authorized to reject this friend request");
        }

        friendship.setStatus(FriendshipStatus.REJECTED);
        friendshipRepository.save(friendship);
    }

    public List<User> getFriends(User user) {
        List<Friendship> friendships = friendshipRepository.findAllAcceptedFriendships(user.getId());
        
        return friendships.stream()
                .map(friendship -> friendship.getUserId().equals(user.getId()) 
                        ? userRepository.findById(friendship.getFriendId()).orElse(null)
                        : userRepository.findById(friendship.getUserId()).orElse(null))
                .filter(friend -> friend != null)
                .collect(Collectors.toList());
    }

    public List<User> getPendingFriendRequests(User user) {
        List<Friendship> pendingRequests = friendshipRepository.findByFriendIdAndStatus(user.getId(), FriendshipStatus.PENDING);
        return pendingRequests.stream()
                .map(friendship -> userRepository.findById(friendship.getUserId()).orElse(null))
                .filter(friend -> friend != null)
                .collect(Collectors.toList());
    }

    public void removeFriend(User user, Long friendId) {
        friendshipRepository.deleteByUserIdAndFriendId(user.getId(), friendId);
        friendshipRepository.deleteByUserIdAndFriendId(friendId, user.getId());
    }

    public boolean areFriends(Long userId1, Long userId2) {
        logger.info("Checking friendship status between users {} and {}", userId1, userId2);
        
        boolean areFriends = friendshipRepository.existsAcceptedFriendship(userId1, userId2);
        logger.info("Friendship status between users {} and {}: {}", userId1, userId2, areFriends);
        
        return areFriends;
    }

    public int getFriendCount(Long userId) {
        return friendshipRepository.countAcceptedFriendships(userId);
    }
} 