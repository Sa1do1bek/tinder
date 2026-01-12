package com.example.tinder.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifyDto {

    @NotBlank
    private String code;

    @NotBlank
    private String email;
    @NotBlank
    private String password;



    @NotBlank
    private String deviceId;

    @NotBlank
    private String username;
}

