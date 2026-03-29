package com.company.ems.employee.dto;

import java.math.BigDecimal;

public record EmployeeFilterRequest(
        String search,
        String department,
        String role,
        String status,
        BigDecimal minSalary,
        BigDecimal maxSalary
) {
}
