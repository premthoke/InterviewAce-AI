package com.interviewace.backend.controller;

import com.interviewace.backend.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ApiResponse health() {

        return new ApiResponse(
                true,
                "InterviewAce Backend is running successfully",
                null
        );

    }

}