package com.example.tinder.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private Long userId;
    private List<String> roles;
    private String ipAddress;
    private String endpoint;
    private String sourceType;
    private String action;
    private String entityName;
    @Column(columnDefinition = "TEXT")
    private String description;
    private LocalDateTime time;
    private Long entityId;
    @Column(columnDefinition = "TEXT")
    private String entitySnapshot;
    private String changes;
}
