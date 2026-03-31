package com.example.tinder.dto.auth;

import com.example.tinder.enums.Gender;

import java.util.Date;

public interface BaseRegisterDto {
    String getUsername();
    String getPassword();
    String getFullName();
    Gender getGender();
    Date getBirthDate();
    String getDeviceId();
}

