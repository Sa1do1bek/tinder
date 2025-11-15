package com.example.tinder.controller;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.service.UserLogService;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/use/log")
public class UserLogController {

    private final UserLogService userLogService;

    public UserLogController(UserLogService userLogService) {
        this.userLogService = userLogService;
    }

    @GetMapping("/get/all/by/filter")
    public ApiResponse getAllByFilter(@RequestParam(required = false) Long userId,
                                      @RequestParam(required = false) String username,
                                      @RequestParam(required = false) String action,
                                      @RequestParam(required = false) String endpoint,
                                      @RequestParam(required = false) String ipAddress,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size){
        return userLogService.getAllUserLogs(userId, username, action, endpoint, ipAddress, PageRequest.of(page, size));
    }

}
