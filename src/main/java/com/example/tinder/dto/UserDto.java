package com.example.tinder.dto;

import com.example.tinder.dto.prefence.PreferenceDto;
import com.example.tinder.enums.Gender;
import com.example.tinder.enums.Interest;
import com.example.tinder.enums.UserRole;
import lombok.*;

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
    private String status;
    private UserRole role;
    private String profilePic;
    private Gender gender;
    private Date birthDate;
    private PreferenceDto preference;
    private List<Interest> interests = new ArrayList<>();
    private String bio;
    private String latitude;
    private String longitude;


}
