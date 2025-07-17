package org.example.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.example.shared.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
      ConstraintViolationException ex, HttpServletRequest request) {
    Map<String, String> errors = new HashMap<>();

    ex.getConstraintViolations()
        .forEach(
            violation -> {
              String path =
                  violation.getPropertyPath().toString().replaceFirst("^publishRecords\\.", "");
              String message = violation.getMessage();

              errors.put(path, message);
            });

    ErrorResponse errorResponse =
        new ErrorResponse(
            "Validation Failed",
            errors,
            request.getRequestURI(),
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            LocalDateTime.now());

    return ResponseEntity.unprocessableEntity().body(errorResponse);
  }

  @ExceptionHandler(TooManyRequestsException.class)
  public ResponseEntity<ErrorResponse> handleTooManyRequestsExceptions(
      TooManyRequestsException ex, HttpServletRequest request) {
    ErrorResponse errorResponse =
        new ErrorResponse(
            "Too Many Requests",
            Map.of("too_many_requests", ex.getMessage()),
            request.getRequestURI(),
            HttpStatus.TOO_MANY_REQUESTS.value(),
            LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .header("Retry-After", ex.getRetryAfterSeconds())
        .body(errorResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleExceptions(Exception ex, HttpServletRequest request) {
    ErrorResponse errorResponse =
        new ErrorResponse(
            "Internal Server Error",
            Map.of("internal_server_error", ex.getMessage()),
            request.getRequestURI(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            LocalDateTime.now());

    return ResponseEntity.internalServerError().body(errorResponse);
  }
}
