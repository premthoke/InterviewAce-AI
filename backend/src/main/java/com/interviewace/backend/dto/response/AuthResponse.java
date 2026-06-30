package com.interviewace.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {

    private boolean success;
    private String message;
    private LocalDateTime timestamp;

    public static AuthResponse success(String message) {
        return new AuthResponse(true, message, LocalDateTime.now());
    }

    public static AuthResponse error(String message) {
        return new AuthResponse(false, message, LocalDateTime.now());
    }

}
