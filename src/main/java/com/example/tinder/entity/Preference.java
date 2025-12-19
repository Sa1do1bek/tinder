package com.example.tinder.entity;

import com.example.tinder.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "preference")
public class Preference extends BaseEntity{
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_preference", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private Gender preferredGender;

    @Column(name = "min_age")
    @Size(min = 18, max = 75)
    private Integer minAge = 18;

    @Column(name = "max_age")
    @Size(min = 23, max = 100)
    private Integer maxAge = 120;

    @Size(min = 2, max = 160)
    private Double distance;
}
