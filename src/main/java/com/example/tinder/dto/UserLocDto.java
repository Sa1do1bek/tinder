package com.example.tinder.dto;

import com.example.tinder.entity.UserInterest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLocDto {
    private Long id;
    private String fullname;
    private String username;
    private String profilePic;
    private String gender;
    private List<UserInterest> userInterests;

}
