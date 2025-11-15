package com.example.tinder.dto;

import com.example.tinder.enums.Gender;
import com.example.tinder.enums.Interest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String fullname;
    private String status;
    private List<String> role = new ArrayList<>();
    private String profilePic;
    private Gender gender;
    private Date birthDate;
    private List<Interest> interests = new ArrayList<>();
    private String bio;
    private String latitude;
    private String longitude;


}
