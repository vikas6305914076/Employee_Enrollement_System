package com.company.ems.employee.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.ems.common.util.SecurityUtils;
import com.company.ems.employee.dto.EmployeeBulkActionResponse;
import com.company.ems.employee.dto.EmployeeBulkStatusUpdateRequest;
import com.company.ems.employee.dto.EmployeeCreateRequest;
import com.company.ems.employee.dto.EmployeeResponse;
import com.company.ems.employee.entity.Employee;
import com.company.ems.employee.entity.EmployeeStatus;
import com.company.ems.employee.entity.UserRole;
import com.company.ems.employee.mapper.EmployeeMapper;
import com.company.ems.employee.repository.EmployeeRepository;
import com.company.ems.exception.DuplicateResourceException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    void createEmployeeShouldPersistRecordWhenPayloadIsValid() {
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "Aarav",
                "Sharma",
                "aarav.sharma",
                "aarav.sharma@company.com",
                "9876543210",
                "Engineering",
                "Software Engineer",
                BigDecimal.valueOf(65000),
                LocalDate.of(2024, 1, 10),
                "Bengaluru",
                "Temp@123",
                "USER"
        );

        Employee mappedEmployee = Employee.builder().username("aarav.sharma").build();
        Employee savedEmployee = Employee.builder()
                .id(1L)
                .firstName("Aarav")
                .lastName("Sharma")
                .username("aarav.sharma")
                .email("aarav.sharma@company.com")
                .phone("9876543210")
                .department("Engineering")
                .role("Software Engineer")
                .userRole(UserRole.USER)
                .salary(BigDecimal.valueOf(65000))
                .joiningDate(LocalDate.of(2024, 1, 10))
                .address("Bengaluru")
                .status(EmployeeStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        EmployeeResponse expectedResponse = new EmployeeResponse(
                1L,
                "Aarav",
                "Sharma",
                "Aarav Sharma",
                "aarav.sharma",
                "aarav.sharma@company.com",
                "9876543210",
                "Engineering",
                "Software Engineer",
                "USER",
                BigDecimal.valueOf(65000),
                LocalDate.of(2024, 1, 10),
                "Bengaluru",
                "ACTIVE",
                null,
                savedEmployee.getCreatedAt(),
                savedEmployee.getUpdatedAt()
        );

        when(employeeRepository.existsByEmailIgnoreCase("aarav.sharma@company.com")).thenReturn(false);
        when(employeeRepository.existsByUsernameIgnoreCase("aarav.sharma")).thenReturn(false);
        when(passwordEncoder.encode("Temp@123")).thenReturn("encoded-password");
        when(employeeMapper.toEntity(request, "encoded-password")).thenReturn(mappedEmployee);
        when(employeeRepository.save(mappedEmployee)).thenReturn(savedEmployee);
        when(employeeMapper.toResponse(savedEmployee)).thenReturn(expectedResponse);
        when(securityUtils.getCurrentUsername()).thenReturn("hr.admin");

        EmployeeResponse response = employeeService.createEmployee(request);

        assertThat(response).isEqualTo(expectedResponse);
        verify(employeeRepository).save(mappedEmployee);
    }

    @Test
    void createEmployeeShouldRejectDuplicateEmail() {
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "Aarav",
                "Sharma",
                "aarav.sharma",
                "aarav.sharma@company.com",
                "9876543210",
                "Engineering",
                "Software Engineer",
                BigDecimal.valueOf(65000),
                LocalDate.of(2024, 1, 10),
                "Bengaluru",
                "Temp@123",
                "USER"
        );

        when(employeeRepository.existsByEmailIgnoreCase("aarav.sharma@company.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Employee email already exists");
    }

    @Test
    void softDeleteEmployeeShouldMarkRecordInactive() {
        Employee employee = Employee.builder()
                .id(99L)
                .firstName("Diya")
                .lastName("Rao")
                .username("diya.rao")
                .status(EmployeeStatus.ACTIVE)
                .userRole(UserRole.USER)
                .build();
        EmployeeResponse expectedResponse = new EmployeeResponse(
                99L,
                "Diya",
                "Rao",
                "Diya Rao",
                "diya.rao",
                null,
                null,
                null,
                null,
                "USER",
                null,
                null,
                null,
                "INACTIVE",
                null,
                null,
                null
        );

        when(employeeRepository.findById(99L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(expectedResponse);
        when(securityUtils.getCurrentUsername()).thenReturn("hr.admin");

        EmployeeResponse response = employeeService.softDeleteEmployee(99L);

        assertThat(response.status()).isEqualTo("INACTIVE");
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.INACTIVE);
    }

    @Test
    void updateEmployeeStatusesShouldApplyStatusToAllSelectedEmployees() {
        EmployeeBulkStatusUpdateRequest request = new EmployeeBulkStatusUpdateRequest(List.of(5L, 8L, 8L), "INACTIVE");
        Employee firstEmployee = Employee.builder()
                .id(5L)
                .status(EmployeeStatus.ACTIVE)
                .userRole(UserRole.USER)
                .build();
        Employee secondEmployee = Employee.builder()
                .id(8L)
                .status(EmployeeStatus.ACTIVE)
                .userRole(UserRole.ADMIN)
                .build();

        when(employeeRepository.findAllById(List.of(5L, 8L))).thenReturn(List.of(firstEmployee, secondEmployee));
        when(employeeMapper.toStatus("INACTIVE")).thenReturn(EmployeeStatus.INACTIVE);
        when(securityUtils.getCurrentUsername()).thenReturn("hr.admin");

        EmployeeBulkActionResponse response = employeeService.updateEmployeeStatuses(request);

        assertThat(response.requestedCount()).isEqualTo(3);
        assertThat(response.updatedCount()).isEqualTo(2);
        assertThat(response.status()).isEqualTo("INACTIVE");
        assertThat(firstEmployee.getStatus()).isEqualTo(EmployeeStatus.INACTIVE);
        assertThat(secondEmployee.getStatus()).isEqualTo(EmployeeStatus.INACTIVE);
        verify(employeeRepository).saveAll(List.of(firstEmployee, secondEmployee));
    }
}
