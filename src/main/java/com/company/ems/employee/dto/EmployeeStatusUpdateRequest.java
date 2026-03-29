package com.company.ems.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmployeeStatusUpdateRequest(
        @NotBlank(message = "Status is required")
        @Pattern(regexp = "(?i)ACTIVE|INACTIVE", message = "Status must be ACTIVE or INACTIVE")
        String status
) {
}
