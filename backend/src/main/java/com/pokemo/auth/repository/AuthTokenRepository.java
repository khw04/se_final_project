package com.pokemo.auth.repository;

import com.pokemo.auth.domain.AuthToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

  Optional<AuthToken> findByToken(String token);
}
