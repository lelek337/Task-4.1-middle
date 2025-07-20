package com.flex.mind.tech.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.flex.mind.tech.model.response.ErrorResponseDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EventConfigAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleEventConfigAlreadyExists(
            EventConfigAlreadyExistsException ex,
            WebRequest request) {

        log.warn("Event configuration already exists: {}", ex.getMessage());

        ErrorResponseDto errorResponse = createErrorResponse(
                "EVENT_CONFIG_ALREADY_EXISTS",
                ex.getMessage(),
                "Configuration with this eventType and source combination already exists",
                request
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(EventConfigNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEventConfigNotFound(
            EventConfigNotFoundException ex,
            WebRequest request) {

        log.warn("Event configuration not found: {}", ex.getMessage());

        ErrorResponseDto errorResponse = createErrorResponse(
                "EVENT_CONFIG_NOT_FOUND",
                ex.getMessage(),
                "No event configuration found with the provided ID",
                request
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationErrors(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        log.warn("Validation error: {}", ex.getMessage());

        String details = ex.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponseDto errorResponse = createErrorResponse(
                "VALIDATION_ERROR",
                "Input validation failed",
                details,
                request
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(
            IllegalArgumentException ex,
            WebRequest request) {

        log.warn("Invalid argument: {}", ex.getMessage());

        ErrorResponseDto errorResponse = createErrorResponse(
                "INVALID_ARGUMENT",
                ex.getMessage(),
                "Invalid request parameter provided",
                request
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneralException(
            Exception ex,
            WebRequest request) {

        log.error("Unexpected error occurred: ", ex);

        ErrorResponseDto errorResponse = createErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                "Please contact system administrator if the problem persists",
                request
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidJson(
            HttpMessageNotReadableException ex,
            WebRequest request) {

        log.warn("Invalid JSON format: {}", ex.getMessage());

        // Извлекаем детали ошибки
        String details = "Invalid JSON format";
        if (ex.getCause() instanceof JsonParseException) {
            JsonParseException jsonEx = (JsonParseException) ex.getCause();
            details = String.format("JSON parse error at line %d, column %d: %s",
                    jsonEx.getLocation().getLineNr(),
                    jsonEx.getLocation().getColumnNr(),
                    jsonEx.getOriginalMessage());
        }

        ErrorResponseDto errorResponse = createErrorResponse(
                "INVALID_JSON_FORMAT",
                "Malformed JSON request",
                details,
                request
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    private ErrorResponseDto createErrorResponse(String code, String message, String details, WebRequest request) {
        return ErrorResponseDto.builder()
                .code(code)
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .path(extractPath(request))
                .build();
    }

    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}