package com.example.tinder.controller;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.UpdateUserGBIDto;
import com.example.tinder.dto.UserDto;
import com.example.tinder.entity.User;
import com.example.tinder.enums.Gender;
import com.example.tinder.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public ApiResponse<?> updateUserNameAndFullName(@AuthenticationPrincipal User user,
                                                 @RequestParam("username") String username,
                                                 @RequestParam("fullname") String fullname) {
        return this.userService.updateUserNameAndFullName(user.getId(), username, fullname);
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
                                      @AuthenticationPrincipal User user) {
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
    public ApiResponse<?> getAllActiveUser(@AuthenticationPrincipal User user,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return this.userService.getAllActiveUser(PageRequest.of(page, size), user.getId());
    }
    @GetMapping("/getRejectedUsers")
    public ApiResponse<?> getAllRejectedUsers(@AuthenticationPrincipal User user,
                                           @RequestParam(defaultValue = "0")int page,
                                           @RequestParam(defaultValue = "5") int size){
        return userService.getAllRejectUsers(PageRequest.of(page,size),user.getId());
    }
    @GetMapping("/getAcceptedUsers")
    public ApiResponse<?> getAllAcceptedUsers(@AuthenticationPrincipal User user,
                                           @RequestParam(defaultValue = "0")int page,
                                           @RequestParam(defaultValue = "5") int size){
        return userService.getAllAcceptUsers(PageRequest.of(page,size),user.getId());
    }

    @GetMapping("/getUserByToken")
    public ApiResponse<?> getUserByToken(@RequestParam("token") String token) {
        return this.userService.getUserByToken(token);
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

    @PutMapping("/updateGenderOrBrithDateOrInterest")
    public ApiResponse<?> updateUserGenderOrBrithdateOrInterest(@AuthenticationPrincipal User user,
                                                             @RequestBody UpdateUserGBIDto dto) {
        return userService.updateUserGBI(user.getId(), dto);
    }

    @DeleteMapping("/deleteUser")
    public ApiResponse<?> deleteUser(@AuthenticationPrincipal User user) {
        return userService.deleteUser(user);
    }

}
