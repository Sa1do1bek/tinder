package com.example.tinder.entity;

import com.example.tinder.enums.CategoryType;
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

    @Enumerated(EnumType.STRING)
    private CategoryType categoryType;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false, unique = true)
    private String name;

    private String nameRu;

    private String nameUz;

    @ManyToOne(fetch = FetchType.LAZY)
    private Preference preferenceLanguage;
}


