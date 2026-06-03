package com.pokemo.auth.api;

import com.pokemo.auth.domain.UserAccount;

public record UserResponse(Long id, String email, String role, boolean emailVerified) {

  public static UserResponse from(UserAccount user) {
    return new UserResponse(user.id(), user.email(), user.role().name(), user.emailVerified());
  }
}
