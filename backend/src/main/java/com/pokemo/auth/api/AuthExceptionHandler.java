package com.pokemo.auth.api;

import com.pokemo.auth.service.AuthException;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

  @ExceptionHandler(AuthException.class)
  ResponseEntity<ApiErrorResponse> handleAuthException(AuthException exception) {
    return ResponseEntity
        .status(exception.status())
        .body(ApiErrorResponse.of(exception.getMessage(), exception.status().value()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
    String message = exception.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));

    if (message.isBlank()) {
      message = "Invalid request";
    }

    return ResponseEntity
        .badRequest()
        .body(ApiErrorResponse.of(message, 400));
  }
}
