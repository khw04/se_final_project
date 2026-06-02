package com.pokemo.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
    return ResponseEntity
        .status(exception.status())
        .body(ErrorResponse.of(exception.getMessage(), exception.status()));
  }
}
