package com.setec.online_survey.security;

import com.setec.online_survey.domain.User;
import com.setec.online_survey.domain.UserRole;
import com.setec.online_survey.features.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
      //  log.info("KKSS"+email);

        User user = userRepository.findUserByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUuid(UUID.randomUUID().toString());
            //newUser.setFullName(oAuth2User.getAttribute("name"));
            newUser.setProvider("GOOGLE");
            newUser.setIsAccountNonExpired(true);
            newUser.setIsAccountNonLocked(true);
            newUser.setIsCredentialsNonExpired(true);
            newUser.setIsDeleted(false);
            newUser.setRole(UserRole.ROLE_USER);
            newUser.setEmailVerified(true);
           // log.info("SKSK"+newUser.getEmail());
            return userRepository.save(newUser);

        });
        return new CustomUserDetails(user, oAuth2User.getAttributes());
    }
}