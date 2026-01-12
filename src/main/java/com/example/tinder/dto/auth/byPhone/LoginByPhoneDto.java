package com.example.tinder.dto.auth.byPhone;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginByPhoneDto {
    @NotBlank(message = "phone cannot be null or empty")
    @Pattern(
            regexp = "^[0-9]{9}$",
            message = "Invalid phone number"
    )
    private String phoneNumber;
    @NotBlank(message = "password cannot be null or empty")
    private String password;
    private String deviceId;
}
