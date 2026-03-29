package com.company.ems.employee.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.ems.common.response.PageResponse;
import com.company.ems.employee.dto.EmployeeFilterRequest;
import com.company.ems.employee.dto.EmployeeResponse;
import com.company.ems.employee.service.EmployeeService;
import com.company.ems.exception.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getEmployeesShouldReturnPaginatedResponse() throws Exception {
        EmployeeFilterRequest filterRequest = new EmployeeFilterRequest(
                "aarav",
                "Engineering",
                "Software",
                "ACTIVE",
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(80000)
        );
        EmployeeResponse employee = new EmployeeResponse(
                11L,
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
                Instant.parse("2026-03-20T07:30:00Z"),
                Instant.parse("2026-03-25T09:00:00Z"),
                Instant.parse("2026-03-25T09:30:00Z")
        );

        PageResponse<EmployeeResponse> pageResponse = new PageResponse<>(
                List.of(employee),
                0,
                10,
                1,
                1,
                "firstName",
                "ASC",
                true,
                true
        );

        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                eq("name"),
                eq("asc"),
                eq(filterRequest)
        )).thenReturn(pageResponse);

        mockMvc.perform(get("/employees")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name")
                        .param("direction", "asc")
                        .param("search", "aarav")
                        .param("department", "Engineering")
                        .param("role", "Software")
                        .param("status", "ACTIVE")
                        .param("minSalary", "50000")
                        .param("maxSalary", "80000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content[0].id").value(11))
                .andExpect(jsonPath("$.data.content[0].fullName").value("Aarav Sharma"))
                .andExpect(jsonPath("$.data.direction").value("ASC"));
    }
}
