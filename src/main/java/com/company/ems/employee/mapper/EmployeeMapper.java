package com.company.ems.employee.mapper;

import com.company.ems.employee.dto.EmployeeCreateRequest;
import com.company.ems.employee.dto.EmployeeResponse;
import com.company.ems.employee.dto.EmployeeUpdateRequest;
import com.company.ems.employee.entity.Employee;
import com.company.ems.employee.entity.EmployeeStatus;
import com.company.ems.employee.entity.UserRole;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public Employee toEntity(EmployeeCreateRequest request, String encodedPassword) {
        return Employee.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .username(request.username().trim())
                .email(request.email().trim())
                .phone(request.phone().trim())
                .department(request.department().trim())
                .role(request.role().trim())
                .salary(request.salary())
                .joiningDate(request.joiningDate())
                .address(request.address().trim())
                .password(encodedPassword)
                .userRole(toUserRole(request.userRole()))
                .status(EmployeeStatus.ACTIVE)
                .build();
    }

    public void applyUpdates(Employee employee, EmployeeUpdateRequest request) {
        employee.setPhone(request.phone().trim());
        employee.setDepartment(request.department().trim());
        employee.setRole(request.role().trim());
        employee.setSalary(request.salary());
        employee.setAddress(request.address().trim());
    }

    public EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getFirstName() + " " + employee.getLastName(),
                employee.getUsername(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getDepartment(),
                employee.getRole(),
                employee.getUserRole().name(),
                employee.getSalary(),
                employee.getJoiningDate(),
                employee.getAddress(),
                employee.getStatus().name(),
                employee.getLastLoginAt(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }

    public EmployeeStatus toStatus(String status) {
        return EmployeeStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
    }

    public UserRole toUserRole(String userRole) {
        return UserRole.valueOf(userRole.trim().toUpperCase(Locale.ROOT));
    }
}
