package com.example.parking.service.impl;

import com.example.parking.entity.UserBot;
import com.example.parking.entity.UserState;
import com.example.parking.repository.UserBotRepository;
import com.example.parking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service("parkingService")
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserBotRepository userRepository;
    
    @Override
    public UserBot createUser(Long chatId, String firstName, String lastName, String username) {
        UserBot user = new UserBot();
        user.setChatId(chatId);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setState(UserState.NEW);
        return userRepository.save(user);
    }
    
    @Override
    public Optional<UserBot> findByChatId(Long chatId) {
        return userRepository.findById(chatId);
    }
    
    @Override
    public Optional<UserBot> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }
    
    @Override
    public UserBot updateUserState(Long chatId, UserState state) {
        UserBot user = userRepository.findById(chatId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setState(state);
        return userRepository.save(user);
    }
    
    @Override
    public UserBot updatePhoneNumber(Long chatId, String phoneNumber) {
        UserBot user = userRepository.findById(chatId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPhoneNumber(phoneNumber);
        user.setState(UserState.REGISTERED);
        return userRepository.save(user);
    }
    
    @Override
    public UserBot updatePrivacySettings(Long chatId, Boolean shareUsername, Boolean sharePhoneNumber) {
        UserBot user = userRepository.findById(chatId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setShareUsername(shareUsername);
        user.setSharePhoneNumber(sharePhoneNumber);
        return userRepository.save(user);
    }
    
    @Override
    public boolean isPhoneNumberExists(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }
}
