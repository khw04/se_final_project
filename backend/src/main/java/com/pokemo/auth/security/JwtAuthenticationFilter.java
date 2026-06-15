package com.pokemo.auth.security;

import com.pokemo.auth.repository.UserAccountRepository;
import com.pokemo.auth.service.JwtTokenService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  private final JwtTokenService jwtTokenService;
  private final UserAccountRepository userAccountRepository;

  public JwtAuthenticationFilter(JwtTokenService jwtTokenService, UserAccountRepository userAccountRepository) {
    this.jwtTokenService = jwtTokenService;
    this.userAccountRepository = userAccountRepository;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = header.substring(7);
    try {
      String email = jwtTokenService.parse(token).getSubject();
      userAccountRepository.findByEmail(email).ifPresentOrElse(user -> {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            user.email(),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name()))
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }, () -> log.warn("JWT authentication failed: token subject has no account, path={}, subject={}",
          request.getRequestURI(), email));
    } catch (JwtException | IllegalArgumentException exception) {
      log.warn("JWT authentication failed: invalid token, path={}, reason={}",
          request.getRequestURI(), exception.getClass().getSimpleName());
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }
}
