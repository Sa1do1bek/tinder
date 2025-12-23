package com.example.tinder.controller;

import com.example.tinder.dto.PreferenceDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.service.PreferenceService;
import com.example.tinder.utils.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/preference")
public class PreferenceController {
    private final PreferenceService preferenceService;


    @PutMapping("/update")
    public ApiResponse<?> updatePreference(@CurrentUser User user,
                                           @RequestBody PreferenceDto.PreferenceUpdateDto dto) {

        return preferenceService.update(user.getId(), dto);
    }


    @GetMapping("/getByUserId")
    public ApiResponse<?> getPreference(@CurrentUser User user) {
        return preferenceService.getByUserId(user.getId());
    }

}
