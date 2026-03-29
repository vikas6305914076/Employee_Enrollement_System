package com.company.ems.config;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.bootstrap-admin")
public record AdminBootstrapProperties(
        boolean enabled,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        @NotBlank String phone,
        @NotBlank String department,
        @NotBlank String role,
        @NotNull @DecimalMin("0.01") BigDecimal salary,
        @NotNull LocalDate joiningDate,
        @NotBlank String address,
        @NotBlank String username,
        @NotBlank String password
) {
}
