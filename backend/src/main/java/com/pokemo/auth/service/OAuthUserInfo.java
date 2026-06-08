package com.pokemo.auth.service;

import com.pokemo.auth.domain.OAuthProvider;

/**
 * 각 소셜 provider 응답을 정규화한 사용자 정보.
 * email은 카카오처럼 미동의 시 null일 수 있다.
 */
public record OAuthUserInfo(
    OAuthProvider provider,
    String providerUserId,
    String email,
    boolean emailVerified,
    String name
) {
}
