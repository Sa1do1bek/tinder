package com.example.tinder.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tokens")
public class Token extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true) // Foydalanuvchi uchun bitta token bo'lishini ta'minlaydi
    private User user;

    @Column(nullable = false, unique = true, length = 512) // JWT tokenlari uzun bo'lishi mumkin
    private String token;

    private String type; // masalan, "Bearer"
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
}
