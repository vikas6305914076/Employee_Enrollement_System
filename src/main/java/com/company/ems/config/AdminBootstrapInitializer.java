package com.company.ems.config;

import com.company.ems.employee.entity.Employee;
import com.company.ems.employee.entity.EmployeeStatus;
import com.company.ems.employee.entity.UserRole;
import com.company.ems.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapInitializer implements ApplicationRunner {

    private final AdminBootstrapProperties adminBootstrapProperties;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!adminBootstrapProperties.enabled()) {
            log.info("Bootstrap admin creation is disabled for the active profile");
            return;
        }

        if (employeeRepository.existsByUsernameIgnoreCase(adminBootstrapProperties.username())) {
            log.info("Bootstrap admin already exists username={}", adminBootstrapProperties.username());
            return;
        }

        Employee adminUser = Employee.builder()
                .firstName(adminBootstrapProperties.firstName())
                .lastName(adminBootstrapProperties.lastName())
                .email(adminBootstrapProperties.email())
                .phone(adminBootstrapProperties.phone())
                .department(adminBootstrapProperties.department())
                .role(adminBootstrapProperties.role())
                .salary(adminBootstrapProperties.salary())
                .joiningDate(adminBootstrapProperties.joiningDate())
                .address(adminBootstrapProperties.address())
                .username(adminBootstrapProperties.username())
                .password(passwordEncoder.encode(adminBootstrapProperties.password()))
                .userRole(UserRole.ADMIN)
                .status(EmployeeStatus.ACTIVE)
                .build();

        employeeRepository.save(adminUser);
        log.info("Bootstrap admin created username={}", adminBootstrapProperties.username());
    }
}
