package com.example.tinder.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "conversation")
public class Conversation extends BaseEntity {

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages;

    @Column(name = "unread_count", columnDefinition = "INT DEFAULT 0")
    private Integer unreadCount = 0;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deletedForUser1 = false;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deletedForUser2 = false;
}
