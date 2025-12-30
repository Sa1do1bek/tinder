package com.example.tinder.dto;

import com.example.tinder.enums.Gender;
import com.example.tinder.enums.UserRole;
import com.example.tinder.enums.UserStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String fullname;
    private UserStatus status;
    private UserRole role;
    private String profilePic;
    private Gender gender;
    private Date birthDate;
    private PreferenceDto preference;
    private String bio;
    private Boolean isOnline;
    private LocalDateTime lastSeen;
    private String latitude;
    private String longitude;
    private Boolean isVerified;


}
