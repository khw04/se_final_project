package com.pokemo.auth.repository;

import com.pokemo.auth.domain.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

  boolean existsByEmail(String email);

  Optional<UserAccount> findByEmail(String email);
}
