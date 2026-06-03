package com.pokemo.notice.domain;

/**
 * 공지 분류 태그. 프론트엔드 union 타입(공지/점검/약관/베타/런칭)과 1:1로 대응한다.
 * JSON 직렬화 시 enum 상수명이 그대로 한국어 문자열로 노출된다.
 */
public enum NoticeTag {
  공지,
  점검,
  약관,
  베타,
  런칭
}
