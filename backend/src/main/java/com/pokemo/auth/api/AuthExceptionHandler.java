package com.pokemo.auth.api;

import com.pokemo.auth.service.AuthException;
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
    return ResponseEntity
        .badRequest()
        .body(ApiErrorResponse.of("Invalid request", 400));
  }
}
