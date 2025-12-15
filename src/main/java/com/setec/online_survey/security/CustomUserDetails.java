package com.setec.online_survey.security;

import com.setec.online_survey.domain.User;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole().getAuthority()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // or username if you prefer
    }

    public String getUuid() {
        return user.getUuid();
    }

    // Optional helper
    public Set<String> getRoles() {
        return Set.of(user.getRole().getAuthority().replace("ROLE_", ""));
    }

    @Override public boolean isAccountNonExpired() { return user.getIsAccountNonExpired(); }
    @Override public boolean isAccountNonLocked() { return user.getIsAccountNonLocked(); }
    @Override public boolean isCredentialsNonExpired() { return user.getIsCredentialsNonExpired(); }
    @Override public boolean isEnabled() { return !user.getIsDeleted(); }
}