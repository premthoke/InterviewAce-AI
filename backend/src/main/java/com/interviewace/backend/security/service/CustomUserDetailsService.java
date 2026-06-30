package com.interviewace.backend.security.service;

import com.interviewace.backend.entity.user.User;
import com.interviewace.backend.repository.user.UserRepository;
import com.interviewace.backend.security.user.CustomUserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Bridges Spring Security's authentication mechanism with the application's User entity.
 * Loads user data from MySQL via UserRepository and wraps it in a CustomUserPrincipal.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by email address. Spring Security calls this method during authentication
     * via DaoAuthenticationProvider.
     *
     * @param email the email address used as the username
     * @return UserDetails wrapping the found User entity
     * @throws UsernameNotFoundException if no user exists with the given email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email
                ));

        return new CustomUserPrincipal(user);
    }

}
