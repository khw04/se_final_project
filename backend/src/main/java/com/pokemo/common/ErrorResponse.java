package com.pokemo.common;

import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;

public record ErrorResponse(String message, int status, OffsetDateTime timestamp) {

  public static ErrorResponse of(String message, HttpStatus status) {
    return new ErrorResponse(message, status.value(), OffsetDateTime.now());
  }
}
