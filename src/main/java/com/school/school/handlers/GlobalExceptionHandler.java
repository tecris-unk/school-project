package com.school.school.handlers;

import com.school.school.exceptions.ConflictException;
import com.school.school.exceptions.NotFoundException;
import com.school.school.exceptions.ResourceNotFoundException;
import com.school.school.exceptions.ValidationException;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@Hidden
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String VALIDATION_FAILED = "Validation failed";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
        final MethodArgumentNotValidException ex,
        final HttpServletRequest request) {

            Map<String, List<String>> fieldErrors = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .collect(Collectors.groupingBy(
                            FieldError::getField,
                            Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                    ));

            return buildResponse(
                    HttpStatus.BAD_REQUEST,
                    VALIDATION_FAILED,
                    "Request validation error",
                    request,
                    fieldErrors,
                    ex
            );
        }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            final ConstraintViolationException ex,
            final HttpServletRequest request) {

        Map<String, List<String>> violations = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.groupingBy(
                        violation -> violation.getPropertyPath().toString(),
                        Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList())
                ));

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                VALIDATION_FAILED,
                "Request parameter validation error",
                request,
                violations,
                ex
        );
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            final ValidationException ex,
            final HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                VALIDATION_FAILED,
                ex.getMessage(),
                request,
                null,
                ex
        );
    }

    @ExceptionHandler({NotFoundException.class, ResourceNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            final RuntimeException ex,
            final HttpServletRequest request) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Resource not found",
                ex.getMessage(),
                request,
                null,
                ex
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            final ConflictException ex,
            final HttpServletRequest request) {

        return buildResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage(),
                request,
                null,
                ex
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(
            final Exception ex,
            final HttpServletRequest request) {

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Unexpected error occurred",
                request,
                null,
                ex
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            final HttpStatus status,
            final String error,
            final String message,
            final HttpServletRequest request,
            final Map<String, ?> details,
            final Exception exception) {

        if (status.is5xxServerError()) {
            log.error("Request '{}' failed with {}", request.getRequestURI(), status.value(), exception);
        } else {
            log.warn("Request '{}' failed with {}: {}", request.getRequestURI(), status.value(), exception.getMessage());
        }

        ErrorResponse response = new ErrorResponse(
                status.value(),
                error,
                message,
                request.getRequestURI(),
                details
        );

        return ResponseEntity.status(status).body(response);
    }
}