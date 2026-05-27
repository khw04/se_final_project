package com.pokemo.auth.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pokemo.jwt")
public record JwtProperties(
    String issuer,
    String secret,
    long accessTokenTtlMinutes,
    long refreshTokenTtlDays
) {
}
