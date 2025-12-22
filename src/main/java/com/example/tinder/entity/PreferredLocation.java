package com.example.tinder.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "preffered_location")
public class PreferredLocation extends BaseEntity {

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Preference preference;
    private String latitude;
    private String longitude;
}

