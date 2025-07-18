package com.example.tinder.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users_location")
public class UserLocation extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true) // 'user_location' o'rniga 'user_id' va 'unique = true' qo'shildi
    private User user;
    private String latitude;
    private String longitude;
}
