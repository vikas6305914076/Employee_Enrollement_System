package com.company.ems.auth.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.ems.auth.dto.LoginRequest;
import com.company.ems.auth.dto.LoginResponse;
import com.company.ems.auth.jwt.JwtService;
import com.company.ems.auth.jwt.JwtToken;
import com.company.ems.common.util.SecurityUtils;
import com.company.ems.employee.entity.Employee;
import com.company.ems.employee.entity.EmployeeStatus;
import com.company.ems.employee.entity.UserRole;
import com.company.ems.employee.repository.EmployeeRepository;
import com.company.ems.exception.AuthenticationException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void loginShouldReturnJwtPayloadForActiveUser() {
        Employee employee = Employee.builder()
                .id(7L)
                .firstName("System")
                .lastName("Admin")
                .username("hr.admin")
                .email("admin@ems.local")
                .userRole(UserRole.ADMIN)
                .status(EmployeeStatus.ACTIVE)
                .createdAt(Instant.parse("2026-03-01T08:00:00Z"))
                .lastLoginAt(Instant.parse("2026-03-10T08:00:00Z"))
                .build();

        when(employeeRepository.findByUsernameIgnoreCase("hr.admin")).thenReturn(Optional.of(employee));
        when(jwtService.generateToken(employee)).thenReturn(new JwtToken("jwt-token", Instant.parse("2026-03-25T10:00:00Z")));

        LoginResponse response = authService.login(new LoginRequest("hr.admin", "Admin@123"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.user().userRole()).isEqualTo("ADMIN");
        assertThat(response.loginWarningMessage()).isEqualTo("Alert: you had not logged in for more than one week.");
        assertThat(employee.getLastLoginAt()).isNotNull();
        verify(employeeRepository).save(employee);
    }

    @Test
    void loginShouldTranslateBadCredentialsIntoDomainException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("wrong.user", "bad-password")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void loginShouldNotReturnWarningForRecentLogin() {
        Employee employee = Employee.builder()
                .id(7L)
                .firstName("System")
                .lastName("Admin")
                .username("hr.admin")
                .email("admin@ems.local")
                .userRole(UserRole.ADMIN)
                .status(EmployeeStatus.ACTIVE)
                .createdAt(Instant.now().minusSeconds(3 * 24 * 60 * 60))
                .lastLoginAt(Instant.now().minusSeconds(2 * 24 * 60 * 60))
                .build();

        when(employeeRepository.findByUsernameIgnoreCase("hr.admin")).thenReturn(Optional.of(employee));
        when(jwtService.generateToken(employee)).thenReturn(new JwtToken("jwt-token", Instant.parse("2026-03-25T10:00:00Z")));

        LoginResponse response = authService.login(new LoginRequest("hr.admin", "Admin@123"));

        assertThat(response.loginWarningMessage()).isNull();
        verify(employeeRepository).save(employee);
    }
}
