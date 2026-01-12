package com.example.tinder.dto.auth.byPhone;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifyByPhoneDto {

    @NotBlank
    private String code;

    @NotBlank(message = "phone cannot be null or empty")
    @Pattern(
            regexp = "^[0-9]{9}$",
            message = "Invalid phone number"
    )
    private String phoneNumber;

    @NotBlank
    private String password;
    
    

    @NotBlank
    private String deviceId;

    @NotBlank
    private String username;
}

