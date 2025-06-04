package ru.utalieva.victorina.model.entity;

/**
 * Enum representing the possible states of a friendship between users.
 */
public enum FriendshipStatus {
    /**
     * A friend request has been sent but not yet accepted
     */
    PENDING,
    
    /**
     * The friend request has been accepted and the friendship is active
     */
    ACCEPTED,
    
    /**
     * The friend request has been rejected
     */
    REJECTED,
    
    /**
     * The user has been blocked
     */
    BLOCKED
} 