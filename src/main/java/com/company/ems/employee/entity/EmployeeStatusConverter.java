package com.company.ems.employee.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter(autoApply = false)
public class EmployeeStatusConverter implements AttributeConverter<EmployeeStatus, String> {

    @Override
    public String convertToDatabaseColumn(EmployeeStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public EmployeeStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String normalized = dbData.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ACTIVE" -> EmployeeStatus.ACTIVE;
            case "INACTIVE" -> EmployeeStatus.INACTIVE;
            case "ACTIVATED" -> EmployeeStatus.ACTIVE;
            case "DEACTIVATED" -> EmployeeStatus.INACTIVE;
            default -> EmployeeStatus.valueOf(normalized);
        };
    }
}
