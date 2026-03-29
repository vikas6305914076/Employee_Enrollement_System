package com.company.ems.employee.repository;

import com.company.ems.employee.entity.Employee;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    public static Specification<Employee> filterBy(
            String search,
            String department,
            String role,
            String status,
            BigDecimal minSalary,
            BigDecimal maxSalary
    ) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (hasText(search)) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("department")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("role")), pattern)
                ));
            }

            if (hasText(department)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("department")),
                        "%" + department.trim().toLowerCase() + "%"
                ));
            }

            if (hasText(role)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("role")),
                        "%" + role.trim().toLowerCase() + "%"
                ));
            }

            if (hasText(status)) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("status").as(String.class)),
                        status.trim().toUpperCase()
                ));
            }

            if (minSalary != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("salary"), minSalary));
            }

            if (maxSalary != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("salary"), maxSalary));
            }

            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
