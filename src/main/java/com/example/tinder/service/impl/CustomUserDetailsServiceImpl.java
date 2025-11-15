package com.example.tinder.service.impl;

import com.example.tinder.entity.User;
import com.example.tinder.exception.ResourceNotFoundException;
import com.example.tinder.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsernameAndActive(username, true)
                .orElseThrow(() -> new UsernameNotFoundException("User not found or disabled"));

        return user;
    }

}