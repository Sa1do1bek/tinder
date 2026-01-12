package com.example.tinder.entity;

import com.example.tinder.enums.Gender;
import com.example.tinder.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "pending_users")
@Entity
public class PendingUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Pattern(
            regexp = "^[0-9]{9}$",
            message = "Invalid phone number"
    )
    private String phoneNumber;

    private String email;
    private String username;
    private String password;
    private String fullName;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Date birthDate;
    private String deviceId;
    private UserRole role;

    private String code;
    private LocalDateTime createdAt;
}




