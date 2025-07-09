package com.example.tinder.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "roles")
public class UserRole extends BaseEntity {

    @Column(unique = true, nullable = false, length = 50)
    private String name;
}
