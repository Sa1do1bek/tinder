package com.example.tinder.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "user_log")
public class UserLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String username;
    private List<String> roles;

    private String action;
    private String endpoint;
    private String ipAddress;
    private String userAgent;

    private LocalDateTime requestTime;
    private LocalDateTime responseTime;
    private Integer statusCode;
    private Boolean success;
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String requestBody;
    @Column(columnDefinition = "TEXT")
    private String responseBody;

    private String source;
}
