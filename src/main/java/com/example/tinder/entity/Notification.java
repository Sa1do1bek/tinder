package com.example.tinder.entity;

import com.example.tinder.enums.NotificationPathType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notification")
public class Notification extends BaseEntity {

    @ManyToOne
    private User user;

    private String title;

    @Column(columnDefinition = "text")
    private String body;

    @Column(columnDefinition = "boolean default false")
    private Boolean read = false;

    @Enumerated(EnumType.STRING)
    private NotificationPathType pathType;
}