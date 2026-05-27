package com.pokemo.auth.security;

import com.pokemo.auth.repository.UserAccountRepository;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountUserDetailsService implements UserDetailsService {

  private final UserAccountRepository userAccountRepository;

  public AccountUserDetailsService(UserAccountRepository userAccountRepository) {
    this.userAccountRepository = userAccountRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) {
    return userAccountRepository.findByEmail(username)
        .map(user -> new User(
            user.email(),
            user.passwordHash(),
            List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name()))
        ))
        .orElseThrow(() -> new UsernameNotFoundException("Email not found"));
  }
}
