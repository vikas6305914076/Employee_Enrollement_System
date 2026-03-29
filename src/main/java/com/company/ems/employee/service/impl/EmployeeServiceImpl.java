package com.company.ems.employee.service.impl;

import com.company.ems.common.response.PageResponse;
import com.company.ems.common.util.PageRequestUtils;
import com.company.ems.common.util.SecurityUtils;
import com.company.ems.employee.dto.EmployeeBulkActionResponse;
import com.company.ems.employee.dto.EmployeeBulkStatusUpdateRequest;
import com.company.ems.employee.dto.EmployeeCreateRequest;
import com.company.ems.employee.dto.EmployeeFilterRequest;
import com.company.ems.employee.dto.EmployeeResponse;
import com.company.ems.employee.dto.EmployeeStatusUpdateRequest;
import com.company.ems.employee.dto.EmployeeUpdateRequest;
import com.company.ems.employee.entity.Employee;
import com.company.ems.employee.entity.EmployeeStatus;
import com.company.ems.employee.mapper.EmployeeMapper;
import com.company.ems.employee.repository.EmployeeRepository;
import com.company.ems.employee.repository.EmployeeSpecifications;
import com.company.ems.employee.service.EmployeeService;
import com.company.ems.exception.DuplicateResourceException;
import com.company.ems.exception.ResourceNotFoundException;
import com.company.ems.exception.ValidationException;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        validateUniqueFields(request);

        Employee employee = employeeMapper.toEntity(request, passwordEncoder.encode(request.password()));
        Employee savedEmployee = employeeRepository.save(employee);

        log.info(
                "Employee created by admin={} employeeId={} username={}",
                securityUtils.getCurrentUsername(),
                savedEmployee.getId(),
                savedEmployee.getUsername()
        );

        return employeeMapper.toResponse(savedEmployee);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> getEmployees(
            int page,
            int size,
            String sortBy,
            String direction,
            EmployeeFilterRequest filterRequest
    ) {
        Pageable pageable = PageRequestUtils.create(page, size, sortBy, direction);
        validateSalaryRange(filterRequest);

        Page<EmployeeResponse> employees = employeeRepository.findAll(
                EmployeeSpecifications.filterBy(
                        filterRequest.search(),
                        filterRequest.department(),
                        filterRequest.role(),
                        filterRequest.status(),
                        filterRequest.minSalary(),
                        filterRequest.maxSalary()
                ),
                pageable
        ).map(employeeMapper::toResponse);
        return PageResponse.from(employees);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long employeeId) {
        return employeeMapper.toResponse(getEmployeeOrThrow(employeeId));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getCurrentEmployee() {
        Employee employee = employeeRepository.findByUsernameIgnoreCase(securityUtils.getCurrentUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated employee record was not found"));
        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long employeeId, EmployeeUpdateRequest request) {
        Employee employee = getEmployeeOrThrow(employeeId);
        employeeMapper.applyUpdates(employee, request);
        Employee savedEmployee = employeeRepository.save(employee);

        log.info("Employee updated by admin={} employeeId={}", securityUtils.getCurrentUsername(), employeeId);
        return employeeMapper.toResponse(savedEmployee);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployeeStatus(Long employeeId, EmployeeStatusUpdateRequest request) {
        Employee employee = getEmployeeOrThrow(employeeId);
        employee.setStatus(employeeMapper.toStatus(request.status()));
        Employee savedEmployee = employeeRepository.save(employee);

        log.info(
                "Employee status updated by admin={} employeeId={} status={}",
                securityUtils.getCurrentUsername(),
                employeeId,
                savedEmployee.getStatus()
        );
        return employeeMapper.toResponse(savedEmployee);
    }

    @Override
    @Transactional
    public EmployeeBulkActionResponse updateEmployeeStatuses(EmployeeBulkStatusUpdateRequest request) {
        List<Long> employeeIds = request.employeeIds().stream().distinct().toList();
        List<Employee> employees = employeeRepository.findAllById(employeeIds);

        if (employees.size() != employeeIds.size()) {
            Set<Long> foundIds = employees.stream().map(Employee::getId).collect(java.util.stream.Collectors.toSet());
            List<Long> missingIds = employeeIds.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new ResourceNotFoundException("Employees not found for ids: " + missingIds);
        }

        EmployeeStatus status = employeeMapper.toStatus(request.status());
        employees.forEach(employee -> employee.setStatus(status));
        employeeRepository.saveAll(employees);

        log.info(
                "Bulk employee status update by admin={} count={} status={}",
                securityUtils.getCurrentUsername(),
                employees.size(),
                status
        );

        return new EmployeeBulkActionResponse(
                request.employeeIds().size(),
                employees.size(),
                status.name(),
                employeeIds
        );
    }

    @Override
    @Transactional
    public EmployeeResponse softDeleteEmployee(Long employeeId) {
        Employee employee = getEmployeeOrThrow(employeeId);
        employee.setStatus(EmployeeStatus.INACTIVE);
        Employee savedEmployee = employeeRepository.save(employee);

        log.info("Employee soft deleted by admin={} employeeId={}", securityUtils.getCurrentUsername(), employeeId);
        return employeeMapper.toResponse(savedEmployee);
    }

    private void validateUniqueFields(EmployeeCreateRequest request) {
        if (employeeRepository.existsByEmailIgnoreCase(request.email().trim())) {
            throw new DuplicateResourceException("Employee email already exists");
        }
        if (employeeRepository.existsByUsernameIgnoreCase(request.username().trim())) {
            throw new DuplicateResourceException("Employee username already exists");
        }
    }

    private Employee getEmployeeOrThrow(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
    }

    private void validateSalaryRange(EmployeeFilterRequest filterRequest) {
        if (filterRequest.minSalary() != null
                && filterRequest.maxSalary() != null
                && filterRequest.minSalary().compareTo(filterRequest.maxSalary()) > 0) {
            throw new ValidationException("Minimum salary cannot be greater than maximum salary");
        }
    }
}
