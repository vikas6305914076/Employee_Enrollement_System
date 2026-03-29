package com.company.ems.employee.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter(autoApply = false)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public UserRole convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String normalized = dbData.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ADMIN" -> UserRole.ADMIN;
            case "USER", "EMPLOYEE" -> UserRole.USER;
            default -> UserRole.valueOf(normalized);
        };
    }
}
