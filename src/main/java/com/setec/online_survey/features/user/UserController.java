package com.setec.online_survey.features.user;

import com.setec.online_survey.features.auth.dto.UserProfileResponse;
import com.setec.online_survey.features.user.dto.UserDeleteRequest;
import com.setec.online_survey.features.user.dto.UserResponse;
import com.setec.online_survey.security.CustomUserDetails;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping("/me")
    public UserProfileResponse getMyProfile(Authentication authentication) {

        if(authentication==null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User unauthorized, please refresh or login again");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        assert userDetails != null;
        return UserProfileResponse.builder()
                .email(userDetails.getUsername())
                .roles(userDetails.getRoles().toString())
                .build();
    }

}
