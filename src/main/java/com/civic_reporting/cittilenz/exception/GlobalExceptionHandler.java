package com.civic_reporting.cittilenz.exception;

import com.civic_reporting.cittilenz.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /* =========================
       400 - Validation Errors
    ========================= */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationErrors(
            MethodArgumentNotValidException ex
    ) {

        Map<String, String> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("Validation error: {}", errors);

        return buildResponse(
                false,
                "Validation failed",
                errors,
                HttpStatus.BAD_REQUEST
        );
    }

    /* =========================
       400 - Illegal Argument
    ========================= */

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(
            IllegalArgumentException ex
    ) {

        log.warn("Bad request: {}", ex.getMessage());

        return buildResponse(
                false,
                ex.getMessage(),
                null,
                HttpStatus.BAD_REQUEST
        );
    }

    /* =========================
       404 - Resource Not Found
    ========================= */

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(
            ResourceNotFoundException ex
    ) {

        log.warn("Resource not found: {}", ex.getMessage());

        return buildResponse(
                false,
                ex.getMessage(),
                null,
                HttpStatus.NOT_FOUND
        );
    }

    /* =========================
       500 - Internal Error
    ========================= */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(
            Exception ex
    ) {

        log.error("Unexpected error occurred", ex);

        return buildResponse(
                false,
                "Internal server error",
                null,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    /* =========================
       Helper Method
    ========================= */

    private ResponseEntity<ApiResponse<Object>> buildResponse(
            boolean success,
            String message,
            Object data,
            HttpStatus status
    ) {

        ApiResponse<Object> response = new ApiResponse<>();
        response.setSuccess(success);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(response, status);
    }
}
