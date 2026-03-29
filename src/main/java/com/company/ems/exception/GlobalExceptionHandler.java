package com.company.ems.exception;

import com.company.ems.common.response.ApiError;
import com.company.ems.common.response.ApiResponse;
import com.company.ems.common.response.ApiResponseFactory;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException exception) {
        return ApiResponseFactory.error(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                List.of(new ApiError(null, exception.getMessage()))
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResourceException(DuplicateResourceException exception) {
        return ApiResponseFactory.error(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                List.of(new ApiError(null, exception.getMessage()))
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException exception) {
        return ApiResponseFactory.error(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                List.of(new ApiError(null, exception.getMessage()))
        );
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(ValidationException exception) {
        return ApiResponseFactory.error(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                List.of(new ApiError(null, exception.getMessage()))
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        List<ApiError> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ApiError(error.getField(), error.getDefaultMessage()))
                .toList();

        return ApiResponseFactory.error(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException exception
    ) {
        List<ApiError> errors = exception.getConstraintViolations()
                .stream()
                .map(violation -> new ApiError(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();

        return ApiResponseFactory.error(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception
    ) {
        return ApiResponseFactory.error(
                HttpStatus.BAD_REQUEST,
                "Malformed request payload",
                List.of(new ApiError(null, "Request body could not be parsed"))
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException exception) {
        return ApiResponseFactory.error(
                HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource",
                List.of(new ApiError(null, exception.getMessage()))
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception exception) {
        log.error("Unhandled exception", exception);
        return ApiResponseFactory.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                List.of(new ApiError(null, "Please contact support if the issue persists"))
        );
    }
}
