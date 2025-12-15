package com.example.tinder.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LastSeen extends BaseEntity {

    @OneToOne(fetch = FetchType.EAGER)
    private User user;
    private LocalDateTime lastSeen;
}
