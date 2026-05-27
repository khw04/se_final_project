package com.pokemo.auth.api;

public record AuthResponse(
    Long userId,
    String email,
    String role,
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresInSeconds
) {
}
