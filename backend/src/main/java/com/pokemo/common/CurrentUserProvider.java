package com.pokemo.common;

import com.pokemo.auth.repository.UserAccountRepository;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

  private final UserAccountRepository userAccountRepository;

  public CurrentUserProvider(UserAccountRepository userAccountRepository) {
    this.userAccountRepository = userAccountRepository;
  }

  public long userId(Principal principal) {
    if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required");
    }

    return userAccountRepository.findByEmail(principal.getName())
        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required"))
        .id();
  }
}
