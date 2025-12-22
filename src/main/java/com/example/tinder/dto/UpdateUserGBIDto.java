package com.example.tinder.dto;

import com.example.tinder.enums.Gender;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserGBIDto {
    private String fullName;
    private Date birthDate;
    private Gender gender;
    private String bio;
}
