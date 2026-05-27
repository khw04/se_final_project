package com.pokemo.auth.service;

import com.pokemo.auth.domain.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

  private final JwtProperties properties;
  private final SecretKey signingKey;

  public JwtTokenService(JwtProperties properties) {
    this.properties = properties;
    this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
  }

  public String createAccessToken(UserAccount user) {
    Instant now = Instant.now();
    Instant expiry = now.plus(accessTokenTtl());
    return Jwts.builder()
        .issuer(properties.issuer())
        .subject(user.email())
        .claim("userId", user.id())
        .claim("role", user.role().name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiry))
        .signWith(signingKey)
        .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser()
        .verifyWith(signingKey)
        .requireIssuer(properties.issuer())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public Duration accessTokenTtl() {
    return Duration.ofMinutes(properties.accessTokenTtlMinutes());
  }

  public Duration refreshTokenTtl() {
    return Duration.ofDays(properties.refreshTokenTtlDays());
  }
}
