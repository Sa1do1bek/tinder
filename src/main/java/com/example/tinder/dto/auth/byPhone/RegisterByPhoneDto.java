package com.example.tinder.dto.auth.byPhone;

import com.example.tinder.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterByPhoneDto {
    @NotBlank(message = "phone cannot be null or empty")
    @Pattern(
            regexp = "^[0-9]{9}$",
            message = "Invalid phone number"
    )
    private String phoneNumber;
    private String username;
    @NotBlank(message = "password cannot be null or empty")
    private String password;
    private String fullName;
    private Gender gender;
    private Date birthDate;
    private String deviceId;
}
