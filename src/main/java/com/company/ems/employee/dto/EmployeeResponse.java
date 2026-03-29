package com.company.ems.employee.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String username,
        String email,
        String phone,
        String department,
        String role,
        String userRole,
        BigDecimal salary,
        LocalDate joiningDate,
        String address,
        String status,
        Instant lastLoginAt,
        Instant createdAt,
        Instant updatedAt
) {
}
