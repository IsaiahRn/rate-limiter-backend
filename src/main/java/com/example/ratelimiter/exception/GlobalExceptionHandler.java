package com.example.ratelimiter.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> ve = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            ve.put(fe.getField(), fe.getDefaultMessage());
        }

        ApiError body = ApiError.builder()
                .error("Bad Request")
                .message("Validation failed")
                .path(req.getRequestURI())
                .status(400)
                .timestamp(Instant.now())
                .validationErrors(ve)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArg(IllegalArgumentException ex, HttpServletRequest req) {
        ApiError body = ApiError.builder()
                .error("Bad Request")
                .message(ex.getMessage())
                .path(req.getRequestURI())
                .status(400)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
