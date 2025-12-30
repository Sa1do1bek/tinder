package com.example.tinder.entity;

import com.example.tinder.enums.MessageType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.ser.Serializers;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "message")
public class Message extends BaseEntity {
    @JoinColumn(columnDefinition = "TEXT")
    private String message;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    @ToString.Exclude
    @JsonManagedReference
    private User sender;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    @ToString.Exclude
    @JsonManagedReference
    private User recipient;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType;

    @Column(name = "is_read", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isRead = false;

    @Column(name = "is_liked", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isLiked = false;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deletedForSender = false;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deletedForRecipient = false;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean edited = false;

    private Long replyId;
}
