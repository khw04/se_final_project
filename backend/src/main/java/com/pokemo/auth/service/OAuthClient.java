package com.pokemo.auth.service;

import com.pokemo.auth.domain.OAuthProvider;

/**
 * 소셜 provider별 인증 클라이언트. authorization code를 토큰으로 교환하고
 * 사용자 정보를 정규화해 반환한다. provider를 추가하려면 이 인터페이스를 구현한다.
 */
public interface OAuthClient {

  OAuthProvider provider();

  OAuthUserInfo fetchUser(String code, String redirectUri);
}
