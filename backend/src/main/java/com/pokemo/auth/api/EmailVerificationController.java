package com.pokemo.auth.api;

import com.pokemo.auth.service.EmailVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/email")
public class EmailVerificationController {

  private final EmailVerificationService emailVerificationService;

  public EmailVerificationController(EmailVerificationService emailVerificationService) {
    this.emailVerificationService = emailVerificationService;
  }

  @PostMapping("/verify-request")
  @ResponseStatus(HttpStatus.ACCEPTED)
  void requestCode(@Valid @RequestBody EmailVerificationRequest request) {
    emailVerificationService.requestCode(request.email());
  }

  @PostMapping("/verify")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void verify(@Valid @RequestBody EmailVerificationConfirmRequest request) {
    emailVerificationService.verify(request.email(), request.code());
  }
}
