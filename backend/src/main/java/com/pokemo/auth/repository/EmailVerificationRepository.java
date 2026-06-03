package com.pokemo.auth.repository;

import com.pokemo.auth.domain.EmailVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

  Optional<EmailVerification> findFirstByEmailAndConsumedFalseOrderByIdDesc(String email);
}
