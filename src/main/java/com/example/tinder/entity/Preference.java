package com.example.tinder.entity;

import com.example.tinder.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.NotEmpty;

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
    @Min(18)
    @Max(75)
    private Integer minAge;

    @Column(name = "max_age")
    @Min(23)
    @Max(100)
    private Integer maxAge;

    @DecimalMin("2.0")
    @DecimalMax("160.0")
    private Double distance;
}
