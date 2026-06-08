package com.pokemo.auth.api;

import jakarta.validation.constraints.NotBlank;

public record OAuthLoginRequest(
    @NotBlank String code,
    @NotBlank String redirectUri
) {
}
