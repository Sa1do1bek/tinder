package com.example.tinder.entity;

import com.example.tinder.enums.Interest;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users_interest")
public class UserInterest extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_interest", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private Interest name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preference_id")
    private Preference preference;
}
