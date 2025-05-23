package com.example.tinder.entity;

import com.example.tinder.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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
    private Integer minAge = 18;

    @Column(name = "max_age")
    private Integer maxAge = 120;

    private Double distance;

    @OneToMany(mappedBy = "preference", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserInterest> userInterests = new ArrayList<>();
}
