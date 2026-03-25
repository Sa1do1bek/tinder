package com.example.tinder.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "user_block",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"blocker_id", "blocked_id"}
    ),
    indexes = {
        @Index(name = "idx_block_blocker_blocked", columnList = "blocker_id, blocked_id"),
        @Index(name = "idx_block_blocked_blocker", columnList = "blocked_id, blocker_id")
    }
)
public class Block extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;
}