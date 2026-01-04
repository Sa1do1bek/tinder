package com.example.tinder.entity;

import com.example.tinder.enums.AuthProvider;
import com.example.tinder.enums.Gender;
import com.example.tinder.enums.UserRole;
import com.example.tinder.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@ToString
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {
    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true)
    private String email;
    private String fullname;

    @Enumerated(EnumType.STRING)
    private UserStatus status;
    private String password;

    @Enumerated(EnumType.STRING)
    private Gender gender;
    private Date birthDate;

    @ManyToMany
    @JsonIgnore
    private List<UserInterest> userInterests = new ArrayList<>();

    @OneToOne
    private Preference preference;

    private Double height; // Bo'yi
    private String jobTitle; // Kasbi
    private String company; // Ish joyi
    private String school; // O'qish joyi

    @Column(columnDefinition = "boolean default false")
    private Boolean isOnline = false;
    private LocalDateTime lastSeen;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProfilePicture> profilePictures;
    private String profilePic;
    private String bio;
    @Column(columnDefinition = "boolean default false")
    private boolean active = false;

    @Column(columnDefinition = "boolean default false")
    private Boolean isVerified = false;

    @Column(columnDefinition = "boolean default true")
    private boolean showProfile = true;

    private String latitude;
    private String longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Column(columnDefinition = "boolean default false")
    private Boolean isApproved = false;

    @Column(unique = true)
    private String firebaseUid;






    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.active;
    }

}
