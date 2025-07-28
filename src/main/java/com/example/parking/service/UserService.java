package com.example.parking.service;


import com.example.parking.entity.UserBot;
import com.example.parking.entity.UserState;

import java.util.Optional;

public interface UserService {
    UserBot createUser(Long chatId, String firstName, String lastName, String username);
    Optional<UserBot> findByChatId(Long chatId);
    Optional<UserBot> findByPhoneNumber(String phoneNumber);
    UserBot updateUserState(Long chatId, UserState state);
    UserBot updatePhoneNumber(Long chatId, String phoneNumber);
    UserBot updatePrivacySettings(Long chatId, Boolean shareUsername, Boolean sharePhoneNumber);
    boolean isPhoneNumberExists(String phoneNumber);
}
