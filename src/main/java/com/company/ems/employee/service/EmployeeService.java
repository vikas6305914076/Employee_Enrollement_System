package com.company.ems.employee.service;

import com.company.ems.common.response.PageResponse;
import com.company.ems.employee.dto.EmployeeBulkActionResponse;
import com.company.ems.employee.dto.EmployeeBulkStatusUpdateRequest;
import com.company.ems.employee.dto.EmployeeCreateRequest;
import com.company.ems.employee.dto.EmployeeFilterRequest;
import com.company.ems.employee.dto.EmployeeResponse;
import com.company.ems.employee.dto.EmployeeStatusUpdateRequest;
import com.company.ems.employee.dto.EmployeeUpdateRequest;

public interface EmployeeService {

    EmployeeResponse createEmployee(EmployeeCreateRequest request);

    PageResponse<EmployeeResponse> getEmployees(int page, int size, String sortBy, String direction, EmployeeFilterRequest filterRequest);

    EmployeeResponse getEmployeeById(Long employeeId);

    EmployeeResponse getCurrentEmployee();

    EmployeeResponse updateEmployee(Long employeeId, EmployeeUpdateRequest request);

    EmployeeResponse updateEmployeeStatus(Long employeeId, EmployeeStatusUpdateRequest request);

    EmployeeBulkActionResponse updateEmployeeStatuses(EmployeeBulkStatusUpdateRequest request);

    EmployeeResponse softDeleteEmployee(Long employeeId);
}
