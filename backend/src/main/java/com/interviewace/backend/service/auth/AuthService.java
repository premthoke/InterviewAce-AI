package com.interviewace.backend.service.auth;

import com.interviewace.backend.dto.request.RegisterRequest;
import com.interviewace.backend.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

}
