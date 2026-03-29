package com.company.ems.employee.dto;

import java.util.List;

public record EmployeeBulkActionResponse(
        int requestedCount,
        int updatedCount,
        String status,
        List<Long> employeeIds
) {
}
