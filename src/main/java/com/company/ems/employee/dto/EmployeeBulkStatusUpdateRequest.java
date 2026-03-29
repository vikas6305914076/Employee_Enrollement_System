package com.company.ems.employee.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record EmployeeBulkStatusUpdateRequest(
        @NotEmpty(message = "At least one employee id is required")
        List<@NotNull(message = "Employee id is required") @Positive(message = "Employee id must be positive") Long> employeeIds,
        @NotNull(message = "Status is required")
        @Pattern(regexp = "(?i)ACTIVE|INACTIVE", message = "Status must be ACTIVE or INACTIVE")
        String status
) {
}
