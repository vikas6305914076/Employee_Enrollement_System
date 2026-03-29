package com.company.ems.auth.controller;

import com.company.ems.auth.dto.AuthenticatedUserResponse;
import com.company.ems.auth.dto.LoginRequest;
import com.company.ems.auth.dto.LoginResponse;
import com.company.ems.auth.service.AuthService;
import com.company.ems.common.response.ApiResponse;
import com.company.ems.common.response.ApiResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "JWT authentication and current-user endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate a user and issue a JWT access token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponseFactory.success(HttpStatus.OK, "Login successful", authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Fetch the currently authenticated user context")
    public ResponseEntity<ApiResponse<AuthenticatedUserResponse>> getCurrentUser() {
        return ApiResponseFactory.success(HttpStatus.OK, "Current user fetched successfully", authService.getCurrentUser());
    }
}
