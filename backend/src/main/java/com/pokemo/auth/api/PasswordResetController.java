package com.pokemo.auth.api;

import com.pokemo.auth.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/password")
public class PasswordResetController {

  private final PasswordResetService passwordResetService;

  public PasswordResetController(PasswordResetService passwordResetService) {
    this.passwordResetService = passwordResetService;
  }

  @PostMapping("/reset-request")
  @ResponseStatus(HttpStatus.ACCEPTED)
  void requestReset(@Valid @RequestBody PasswordResetRequest request) {
    passwordResetService.requestReset(request.email());
  }

  @PostMapping("/reset-validate")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void validate(@Valid @RequestBody PasswordResetValidateRequest request) {
    passwordResetService.validateToken(request.token());
  }

  @PostMapping("/reset")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void reset(@Valid @RequestBody PasswordResetConfirmRequest request) {
    passwordResetService.resetPassword(request.token(), request.newPassword());
  }
}
