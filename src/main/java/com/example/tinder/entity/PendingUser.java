package com.example.tinder.entity;

import com.example.tinder.enums.Gender;
import jakarta.persistence.*;
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
public class PendingUser extends BaseEntity{

    private String email;
    private String username;
    private String password;
    private String fullName;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Date birthDate;
    private String deviceId;
    private String role;

    private String code;
}




