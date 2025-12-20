package com.setec.online_survey.features.user;

import com.setec.online_survey.features.user.dto.UserDeleteRequest;
import com.setec.online_survey.features.user.dto.UserResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @DeleteMapping
    public void deleteUser(@RequestBody UserDeleteRequest userDeleteRequest){
        userService.deleteUserByEmail(userDeleteRequest.email());
    }

    @GetMapping
    public List<UserResponse> getAllUser(){
        return userService.getAllUser();
    }

}
