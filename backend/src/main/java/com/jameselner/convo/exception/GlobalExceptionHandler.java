package com.jameselner.convo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TYPE_BASE = "https://convo.app/errors/";

    @ExceptionHandler(ConvoException.class)
    public ResponseEntity<ProblemDetail> handleConvoException(ConvoException ex) {
        log.warn("Application exception: {} - {}", ex.getErrorCode(), ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            ex.getStatus(),
            ex.getMessage()
        );
        problem.setTitle(formatTitle(ex.getErrorCode()));
        problem.setType(URI.create(TYPE_BASE + ex.getErrorCode().toLowerCase().replace("_", "-")));
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(ex.getStatus()).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Request validation failed"
        );
        problem.setTitle("Validation Error");
        problem.setType(URI.create(TYPE_BASE + "validation-error"));
        problem.setProperty("errorCode", "VALIDATION_ERROR");
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("fieldErrors", fieldErrors);

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            "You do not have permission to access this resource"
        );
        problem.setTitle("Access Denied");
        problem.setType(URI.create(TYPE_BASE + "access-denied"));
        problem.setProperty("errorCode", "ACCESS_DENIED");
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(Exception ex) {
        log.error("Unexpected error occurred", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Please try again later."
        );
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create(TYPE_BASE + "internal-error"));
        problem.setProperty("errorCode", "INTERNAL_ERROR");
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.internalServerError().body(problem);
    }

    private String formatTitle(String errorCode) {
        String[] words = errorCode.toLowerCase().split("_");
        StringBuilder title = new StringBuilder();
        for (String word : words) {
            if (!title.isEmpty()) {
                title.append(" ");
            }
            title.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return title.toString();
    }
}
