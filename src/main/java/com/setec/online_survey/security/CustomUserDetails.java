package com.setec.online_survey.security;

import com.setec.online_survey.domain.User;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CustomUserDetails implements UserDetails, OAuth2User {
    private User user;
    private Map<String, Object> attributes;

    public CustomUserDetails(User user) { this.user = user; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole().getAuthority()));
    }
    @Override public String getPassword() { return user.getPassword(); }
    @Override public String getUsername() { return user.getEmail(); }
    @Override public String getName() { return user.getEmail(); }
    @Override public Map<String, Object> getAttributes() { return attributes; }
    public String getUuid() { return user.getUuid(); }

    @Override public boolean isAccountNonExpired() { return user.getIsAccountNonExpired(); }
    @Override public boolean isAccountNonLocked() { return user.getIsAccountNonLocked(); }
    @Override public boolean isCredentialsNonExpired() { return user.getIsCredentialsNonExpired(); }
    @Override public boolean isEnabled() { return !user.getIsDeleted(); }
}