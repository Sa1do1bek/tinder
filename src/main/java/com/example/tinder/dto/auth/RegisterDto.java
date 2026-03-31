package com.example.tinder.dto.auth;

import com.example.tinder.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterDto implements BaseRegisterDto{
    @NotBlank(message = "email cannot be null or empty")
    @Email
    private String email;
    private String username;
    @NotBlank(message = "password cannot be null or empty")
    private String password;
    private String fullName;
    private Gender gender;
    private Date birthDate;
    private String deviceId;
}
