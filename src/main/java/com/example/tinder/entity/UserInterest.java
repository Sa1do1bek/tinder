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

    private Long userId;

    @Enumerated(EnumType.STRING)
    private CategoryType categoryType;

    private String code;

    private String name;
}
