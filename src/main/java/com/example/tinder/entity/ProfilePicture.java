package com.example.tinder.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "picture")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfilePicture extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String cloudPath;
}
