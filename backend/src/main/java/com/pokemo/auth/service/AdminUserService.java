package com.pokemo.auth.service;

import com.pokemo.auth.api.UserResponse;
import com.pokemo.auth.repository.UserAccountRepository;
import com.pokemo.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

  private final UserAccountRepository userAccountRepository;

  public AdminUserService(UserAccountRepository userAccountRepository) {
    this.userAccountRepository = userAccountRepository;
  }

  @Transactional
  public UserResponse promoteToAdmin(Long userId) {
    var user = userAccountRepository.findById(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    user.promoteToAdmin();
    return UserResponse.from(user);
  }
}
