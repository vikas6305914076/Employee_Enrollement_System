package com.company.ems.common.util;

import com.company.ems.exception.ValidationException;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageRequestUtils {

    private static final int MAX_PAGE_SIZE = 100;

    private static final Map<String, String> SORT_FIELDS = Map.ofEntries(
            Map.entry("name", "firstName"),
            Map.entry("firstname", "firstName"),
            Map.entry("lastname", "lastName"),
            Map.entry("email", "email"),
            Map.entry("department", "department"),
            Map.entry("role", "role"),
            Map.entry("salary", "salary"),
            Map.entry("joiningdate", "joiningDate"),
            Map.entry("status", "status"),
            Map.entry("createdat", "createdAt"),
            Map.entry("updatedat", "updatedAt")
    );

    private PageRequestUtils() {
    }

    public static Pageable create(int page, int size, String sortBy, String direction) {
        if (page < 0) {
            throw new ValidationException("Page index must be zero or greater");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ValidationException("Page size must be between 1 and 100");
        }

        String resolvedSortBy = resolveSortProperty(sortBy);
        Sort.Direction resolvedDirection = resolveDirection(sortBy, direction);

        return PageRequest.of(page, size, Sort.by(resolvedDirection, resolvedSortBy));
    }

    private static String resolveSortProperty(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "createdAt";
        }

        String normalized = normalize(sortBy);
        String property = SORT_FIELDS.get(normalized);
        if (property == null) {
            throw new ValidationException("Unsupported sort field: " + sortBy);
        }
        return property;
    }

    private static Sort.Direction resolveDirection(String sortBy, String direction) {
        if (direction == null || direction.isBlank()) {
            return (sortBy == null || sortBy.isBlank()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        }

        try {
            return Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Unsupported sort direction: " + direction);
        }
    }

    private static String normalize(String value) {
        return value.replaceAll("[^A-Za-z]", "").toLowerCase(Locale.ROOT);
    }
}
