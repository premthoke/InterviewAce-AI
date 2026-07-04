package com.interviewace.backend.service.auth;

import com.interviewace.backend.dto.request.LoginRequest;
import com.interviewace.backend.dto.request.RegisterRequest;
import com.interviewace.backend.dto.response.AuthResponse;
import com.interviewace.backend.entity.user.User;
import com.interviewace.backend.enums.Role;
import com.interviewace.backend.exception.EmailAlreadyExistsException;
import com.interviewace.backend.exception.InvalidCredentialsException;
import com.interviewace.backend.repository.user.UserRepository;
import com.interviewace.backend.security.jwt.JwtTokenService;
import com.interviewace.backend.security.util.JwtConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {

        // Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Check email uniqueness
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        // Build and save user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setIsVerified(false);

        userRepository.save(user);

        return AuthResponse.success("Registration successful");
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Extract the already-loaded principal — no additional database query
        UserDetails principal = (UserDetails) authentication.getPrincipal();

        // Generate JWT
        String token = jwtTokenService.generateToken(principal);

        log.info("User authenticated successfully: email={}", principal.getUsername());

        return AuthResponse.loginSuccess(
                "Authentication successful",
                token,
                JwtConstants.TOKEN_TYPE,
                jwtTokenService.getExpirationMillis()
        );
    }

}
