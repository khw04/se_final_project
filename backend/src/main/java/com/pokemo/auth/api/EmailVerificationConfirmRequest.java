package com.pokemo.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailVerificationConfirmRequest(
    @Email @NotBlank String email,
    @NotBlank String code
) {
}
