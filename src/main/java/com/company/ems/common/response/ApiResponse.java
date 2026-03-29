package com.company.ems.common.response;

import java.time.Instant;
import java.util.List;

public record ApiResponse<T>(
        Instant timestamp,
        int status,
        String message,
        T data,
        List<ApiError> errors
) {
}
