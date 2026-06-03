package com.pokemo.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
    @Email @NotBlank String email
) {
}
