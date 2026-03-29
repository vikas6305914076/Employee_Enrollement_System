package com.company.ems.employee.controller;

import com.company.ems.common.response.ApiResponse;
import com.company.ems.common.response.ApiResponseFactory;
import com.company.ems.common.response.PageResponse;
import com.company.ems.employee.dto.EmployeeBulkActionResponse;
import com.company.ems.employee.dto.EmployeeBulkStatusUpdateRequest;
import com.company.ems.employee.dto.EmployeeCreateRequest;
import com.company.ems.employee.dto.EmployeeFilterRequest;
import com.company.ems.employee.dto.EmployeeResponse;
import com.company.ems.employee.dto.EmployeeStatusUpdateRequest;
import com.company.ems.employee.dto.EmployeeUpdateRequest;
import com.company.ems.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "Admin-managed employee enrollment and maintenance APIs")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @Operation(summary = "Create a new employee record")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
        return ApiResponseFactory.success(HttpStatus.CREATED, "Employee created successfully", employeeService.createEmployee(request));
    }

    @PostMapping("/enroll")
    @Operation(summary = "Backward-compatible alias for employee creation")
    public ResponseEntity<ApiResponse<EmployeeResponse>> enrollEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
        return createEmployee(request);
    }

    @GetMapping
    @Operation(summary = "Fetch employees with pagination and sorting")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> getEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary
    ) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Employees fetched successfully",
                employeeService.getEmployees(
                        page,
                        size,
                        sort,
                        direction,
                        new EmployeeFilterRequest(search, department, role, status, minSalary, maxSalary)
                )
        );
    }

    @GetMapping("/{employeeId}")
    @Operation(summary = "Fetch a single employee by id")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(@PathVariable Long employeeId) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Employee fetched successfully",
                employeeService.getEmployeeById(employeeId)
        );
    }

    @GetMapping("/me")
    @Operation(summary = "Fetch the profile of the currently authenticated employee")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getCurrentEmployee() {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Employee profile fetched successfully",
                employeeService.getCurrentEmployee()
        );
    }

    @PutMapping("/{employeeId}")
    @Operation(summary = "Update employee contact and employment details")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long employeeId,
            @Valid @RequestBody EmployeeUpdateRequest request
    ) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Employee updated successfully",
                employeeService.updateEmployee(employeeId, request)
        );
    }

    @PatchMapping("/{employeeId}/status")
    @Operation(summary = "Update employee active or inactive status")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployeeStatus(
            @PathVariable Long employeeId,
            @Valid @RequestBody EmployeeStatusUpdateRequest request
    ) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Employee status updated successfully",
                employeeService.updateEmployeeStatus(employeeId, request)
        );
    }

    @PatchMapping("/status/bulk")
    @Operation(summary = "Update status for multiple employees in one request")
    public ResponseEntity<ApiResponse<EmployeeBulkActionResponse>> updateEmployeeStatuses(
            @Valid @RequestBody EmployeeBulkStatusUpdateRequest request
    ) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Employee statuses updated successfully",
                employeeService.updateEmployeeStatuses(request)
        );
    }

    @DeleteMapping("/{employeeId}")
    @Operation(summary = "Soft delete an employee by marking the record as inactive")
    public ResponseEntity<ApiResponse<EmployeeResponse>> softDeleteEmployee(@PathVariable Long employeeId) {
        return ApiResponseFactory.success(
                HttpStatus.OK,
                "Employee marked as inactive successfully",
                employeeService.softDeleteEmployee(employeeId)
        );
    }
}
