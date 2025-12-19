package com.example.tinder.dto;

import com.example.tinder.enums.Gender;
import com.example.tinder.enums.Interest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserGBIDto {
    private Date birthDate;
    private Gender gender;
    @Schema(description = "List of interests", implementation = Interest.class)
    private List<Interest> interests=new ArrayList<>();
    private String bio;
}
