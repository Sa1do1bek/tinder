package com.example.tinder.dto;

import com.example.tinder.entity.UserInterest;
import com.example.tinder.enums.Gender;
import com.example.tinder.enums.Interest;
import com.example.tinder.enums.UserRole;
import com.example.tinder.enums.UserStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private String latitude;
    private String longitude;


}
