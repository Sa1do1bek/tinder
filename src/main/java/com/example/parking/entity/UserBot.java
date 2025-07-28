package com.example.parking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "telegram_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBot {
    @Id
    private Long chatId;
    
    @Column(nullable = false)
    private String firstName;
    
    private String lastName;
    
    private String username;
    
    @Column(unique = true)
    private String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    private UserState state = UserState.NEW;
    
    private LocalDateTime registeredAt;
    
    private Boolean shareUsername = true;
    
    private Boolean sharePhoneNumber = false;
    
    private Boolean doNotDisturb = false;
    
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Car> cars;
    
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContactRequest> sentRequests;
    
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContactRequest> receivedRequests;
    
    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
    }
}
