package com.company.ems.employee.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record EmployeeUpdateRequest(
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
        String phone,
        @NotBlank(message = "Department is required")
        @Size(max = 100, message = "Department must not exceed 100 characters")
        String department,
        @NotBlank(message = "Role is required")
        @Size(max = 100, message = "Role must not exceed 100 characters")
        String role,
        @NotNull(message = "Salary is required")
        @DecimalMin(value = "0.01", message = "Salary must be greater than zero")
        BigDecimal salary,
        @NotBlank(message = "Address is required")
        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address
) {
}
