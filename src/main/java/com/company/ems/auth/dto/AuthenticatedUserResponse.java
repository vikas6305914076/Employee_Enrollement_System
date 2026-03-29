package com.company.ems.auth.dto;

public record AuthenticatedUserResponse(
        Long employeeId,
        String username,
        String fullName,
        String email,
        String userRole,
        String status
) {
}
