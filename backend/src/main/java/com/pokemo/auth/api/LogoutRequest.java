package com.pokemo.auth.api;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
    @NotBlank String refreshToken
) {
}
