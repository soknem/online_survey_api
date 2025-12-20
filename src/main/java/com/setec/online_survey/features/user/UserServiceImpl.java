package com.setec.online_survey.features.user;

import com.setec.online_survey.domain.User;
import com.setec.online_survey.features.user.dto.UserResponse;
import com.setec.online_survey.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.parser.HttpParser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse getUserByEmail(String email) {
        return null;
    }

    @Override
    public List<UserResponse> getAllUser() {


        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @Override
    public void deleteUserByEmail(String email) {
        User user = userRepository.findUserByEmail(email).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("user = %s has not been found",email)));

        userRepository.delete(user);
    }

    @Override
    public void disableUserByEmail(String email) {

    }

    @Override
    public void enableUserByEmail(String email) {

    }
}
