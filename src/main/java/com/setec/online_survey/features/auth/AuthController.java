package com.setec.online_survey.features.auth;

import com.setec.online_survey.features.auth.dto.*;
import com.setec.online_survey.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/me")
    public UserProfileResponse getMyProfile(Authentication authentication) {

        if(authentication==null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User unauthorized");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        assert userDetails != null;
        return UserProfileResponse.builder()
                .email(userDetails.getUsername())
                .roles(userDetails.getRoles().toString())
                .build();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    public void register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request,
                              HttpServletResponse response) { // <--- MODIFIED
        return authService.login(request, response);
    }

    @PostMapping("/refresh") // <--- No @RequestBody annotation
    public AuthResponse refresh(HttpServletRequest request, // <--- Accepts request to read cookie
                                HttpServletResponse response) {
        return authService.refresh(request, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication, HttpServletResponse response) {
        // 1. Clear local cookies (Your existing logic)
        clearCookie(response, "access_token");
        clearCookie(response, "refresh_token");

        // 2. Check if the user is a Google user
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.oidc.user.OidcUser) {
            // Redirect to Google Logout if they are an OIDC user
            // This clears the Google session and brings them back to your frontend
            String googleLogoutUrl = "https://accounts.google.com/Logout?continue=https://appengine.google.com/_ah/logout?continue=http://localhost:3000";
            return ResponseEntity.status(HttpStatus.SEE_OTHER).header(HttpHeaders.LOCATION, googleLogoutUrl).build();
        }

        // 3. If standard user (Email/Password), just return OK
        return ResponseEntity.ok().build();
    }

    // Helper method to keep controller clean
    private void clearCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(false) // Set to true in production
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}