package com.example.tinder.dto.auth;

import com.example.tinder.dto.UserDto;
import lombok.*;


@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private int code;
    private String message;
    private String  token;
    private UserDto user;
}
