package com.company.ems.auth.service.impl;

import com.company.ems.auth.dto.AuthenticatedUserResponse;
import com.company.ems.auth.dto.LoginRequest;
import com.company.ems.auth.dto.LoginResponse;
import com.company.ems.auth.jwt.JwtService;
import com.company.ems.auth.jwt.JwtToken;
import com.company.ems.auth.service.AuthService;
import com.company.ems.common.util.SecurityUtils;
import com.company.ems.employee.entity.Employee;
import com.company.ems.employee.entity.EmployeeStatus;
import com.company.ems.employee.repository.EmployeeRepository;
import com.company.ems.exception.AuthenticationException;
import com.company.ems.exception.ResourceNotFoundException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim();

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, request.password()));
        } catch (DisabledException exception) {
            log.warn("Inactive user attempted login username={}", username);
            throw new AuthenticationException("Employee account is inactive");
        } catch (BadCredentialsException exception) {
            log.warn("Invalid login attempt username={}", username);
            throw new AuthenticationException("Invalid username or password");
        }

        Employee employee = employeeRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for username: " + username));

        if (!EmployeeStatus.ACTIVE.equals(employee.getStatus())) {
            throw new AuthenticationException("Employee account is inactive");
        }

        Instant now = Instant.now();
        Instant previousLoginAt = employee.getLastLoginAt();
        String loginWarningMessage = buildLoginWarningMessage(employee, previousLoginAt, now);

        employee.setLastLoginAt(now);
        employeeRepository.save(employee);

        JwtToken jwtToken = jwtService.generateToken(employee);
        log.info("User logged in successfully username={} role={}", employee.getUsername(), employee.getUserRole());

        return new LoginResponse(
                jwtToken.accessToken(),
                "Bearer",
                jwtToken.expiresAt(),
                toAuthenticatedUserResponse(employee),
                loginWarningMessage
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticatedUserResponse getCurrentUser() {
        Employee employee = employeeRepository.findByUsernameIgnoreCase(securityUtils.getCurrentUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated employee record was not found"));
        return toAuthenticatedUserResponse(employee);
    }

    private AuthenticatedUserResponse toAuthenticatedUserResponse(Employee employee) {
        return new AuthenticatedUserResponse(
                employee.getId(),
                employee.getUsername(),
                employee.getFirstName() + " " + employee.getLastName(),
                employee.getEmail(),
                employee.getUserRole().name(),
                employee.getStatus().name()
        );
    }

    private String buildLoginWarningMessage(Employee employee, Instant previousLoginAt, Instant now) {
        Instant inactivityReference = previousLoginAt != null ? previousLoginAt : employee.getCreatedAt();
        if (inactivityReference == null || !inactivityReference.isBefore(now.minus(7, ChronoUnit.DAYS))) {
            return null;
        }

        if (previousLoginAt != null) {
            return "Alert: you had not logged in for more than one week.";
        }

        return "Alert: this account had not been used for more than one week after it was created.";
    }
}
