package com.pokemo.auth.api;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetValidateRequest(
    @NotBlank String token
) {
}
