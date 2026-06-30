package com.interviewace.backend.security.user;

import com.interviewace.backend.entity.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Wraps the User entity to integrate with Spring Security's authentication mechanism.
 * Authorities are derived from the User's Role enum, prefixed with "ROLE_".
 */
public class CustomUserPrincipal implements UserDetails {

    private static final String ROLE_PREFIX = "ROLE_";

    private final User user;

    public CustomUserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(ROLE_PREFIX + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Spring Security uses "username" as the identity field.
     * In this application, the user's email serves as the username.
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Returns true unconditionally until email verification is implemented.
     * When email verification is added, this should delegate to user.getIsVerified().
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    public User getUser() {
        return user;
    }

}
