package com.company.ems.common.response;

import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class ApiResponseFactory {

    private ApiResponseFactory() {
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status).body(body(status, message, data, List.of()));
    }

    public static ResponseEntity<ApiResponse<Void>> error(HttpStatus status, String message, List<ApiError> errors) {
        return ResponseEntity.status(status).body(body(status, message, null, errors));
    }

    public static <T> ApiResponse<T> body(HttpStatus status, String message, T data, List<ApiError> errors) {
        return new ApiResponse<>(
                Instant.now(),
                status.value(),
                message,
                data,
                errors == null ? List.of() : List.copyOf(errors)
        );
    }
}
