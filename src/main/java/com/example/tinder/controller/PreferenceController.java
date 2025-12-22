package com.example.tinder.controller;

import com.example.tinder.dto.PreferenceDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.service.PreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/preference")
public class PreferenceController {
    private final PreferenceService preferenceService;


    @PutMapping("/update")
    public ApiResponse<?> updatePreference(@RequestParam("userId") Long userId,
                                           @RequestBody PreferenceDto.PreferenceUpdateDto dto) {

        return preferenceService.update(userId, dto);
    }


    @GetMapping("/getByUserId")
    public ApiResponse<?> getPreference(@RequestParam("userId") Long userId) {
        return preferenceService.getByUserId(userId);
    }

}
