package com.example.tinder.controller;


import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.service.BlockService;
import com.example.tinder.utils.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/block")
public class BlockController {
    private final BlockService blockService;


    @PostMapping("/create")
    public ApiResponse<?> block(@CurrentUser User user,
                            @RequestParam("blockedUserId") Long blockedUserId ) {

        return ApiResponse.success(
                blockService.blockUser(user.getId(), blockedUserId)
        );
    }


    @DeleteMapping("/unblock")
    public ApiResponse<?> unblock(@CurrentUser User user,
                                  @RequestParam("blockedUserId") Long blockedUserId){

        return ApiResponse.success(blockService.unblockUser(user.getId(), blockedUserId));
    }
}
