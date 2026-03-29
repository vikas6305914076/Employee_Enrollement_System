package com.company.ems.auth.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.ems.auth.dto.AuthenticatedUserResponse;
import com.company.ems.auth.dto.LoginRequest;
import com.company.ems.auth.dto.LoginResponse;
import com.company.ems.auth.service.AuthService;
import com.company.ems.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void loginShouldReturnWrappedJwtResponse() throws Exception {
        LoginResponse loginResponse = new LoginResponse(
                "jwt-token",
                "Bearer",
                Instant.parse("2026-03-25T10:00:00Z"),
                new AuthenticatedUserResponse(1L, "hr.admin", "System Admin", "admin@ems.local", "ADMIN", "ACTIVE"),
                "Alert: you had not logged in for more than one week."
        );

        when(authService.login(new LoginRequest("hr.admin", "Admin@123"))).thenReturn(loginResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("hr.admin", "Admin@123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.data.user.userRole").value("ADMIN"))
                .andExpect(jsonPath("$.data.loginWarningMessage").value("Alert: you had not logged in for more than one week."));
    }
}
