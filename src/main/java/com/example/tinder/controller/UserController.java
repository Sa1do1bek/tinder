package com.example.tinder.controller;

import com.example.tinder.config.WebSocketEventListener;
import com.example.tinder.dto.UserDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.UpdateUserGBIDto;
import com.example.tinder.entity.User;
import com.example.tinder.enums.Gender;
import com.example.tinder.service.UserService;
import com.example.tinder.utils.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/getUserById")
    public ApiResponse<?> getUserById(@RequestParam("id") Long id) {
        return this.userService.getUserById(id);
    }


    @PutMapping("/updateUserNameAndFullName")
    public ApiResponse<?> updateUserNameAndFullName(@RequestParam("userId") Long userId,
                                                 @RequestParam("username") String username,
                                                 @RequestParam("fullname") String fullname) {
        return this.userService.updateUserNameAndFullName(userId, username, fullname);
    }
    @PutMapping("/updateEmail")
    public ApiResponse<?> updateEmail(@RequestParam("email") String email,
                                   @RequestParam("newEmail") String newEmail) {
        return this.userService.updateEmail(email, newEmail);
    }




    @PostMapping("/checkUpdatedEmail")
    public ApiResponse<?> checkUpdatedEmail(@RequestParam("oldEmail") String oldEmail,
                                         @RequestParam("newEmail") String newEmail,
                                         @RequestParam("code") String code) {
        return this.userService.checkUpdatedEmail(oldEmail, newEmail, code);
    }

    @PutMapping("/updatePassword")
    public ApiResponse<?> updatePassword(@RequestParam String oldPassword,
                                      @RequestParam String newPassword,
                                      @CurrentUser User user) {
        return userService.updatePassword(user, oldPassword, newPassword);
    }

    @PostMapping("/checkUpdatedPassword")
    public ApiResponse<?> checkUpdatedPassword(@RequestParam("email") String email,
                                            @RequestParam("password") String password,
                                            @RequestParam("code") String code) {
        return this.userService.checkUpdatedPassword(email, password, code);
    }
    @PostMapping("/checkUsername")
    public ApiResponse<?> checkUsername(@RequestParam("username") String username) {
        return this.userService.checkUsername(username);
    }
    @GetMapping("/getAllActiveUser")
    public ApiResponse<?> getAllActiveUser(@CurrentUser User user,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return this.userService.getAllActiveUser(PageRequest.of(page, size), user.getId());
    }


    @PostMapping("/googleAuth")
    public ApiResponse<?> googleAuth(@RequestParam("email") String email,
                                  @RequestParam("fullName") String fullName,
                                  @RequestParam("picPath") String picPath) {
        return userService.googleAuth(email, fullName, picPath);
    }

    @GetMapping("/search")
    public ApiResponse<?> search(@RequestParam(required = false)String username, @RequestParam(required = false) Gender gender){
        return userService.search(username,gender);
    }

    @PutMapping("/updateGenderOrBrithDate")
    public ApiResponse<?> updateUserGenderOrBrithdate(@CurrentUser User user,
                                                      @RequestBody UpdateUserGBIDto dto) {
        return userService.updateUserGBI(user.getId(), dto);
    }


    @DeleteMapping("/deleteUser")
    public ApiResponse<?> deleteUser(@RequestParam("userId") String userId) {
        return userService.deleteUser(userId);
    }

    @GetMapping("/me")
    public ApiResponse<?> me(@CurrentUser User user) {
        return userService.me(user);
    }


    @PutMapping("/updateUsername")
    public ApiResponse<?> updateUsername(@CurrentUser User user,
                                         @RequestBody String username) {
        return userService.updateUsername(user, username);
    }

    @GetMapping("/online")
    public ApiResponse<?> getOnlineUsers() {
       return userService.getOnlineUsers();
    }



}
