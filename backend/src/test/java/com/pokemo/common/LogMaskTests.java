package com.pokemo.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LogMaskTests {

  @Test
  void masksLocalPartButKeepsDomain() {
    assertThat(LogMask.email("student@example.com")).isEqualTo("s***@example.com");
  }

  @Test
  void handlesNullAndBlank() {
    assertThat(LogMask.email(null)).isEqualTo("(none)");
    assertThat(LogMask.email("  ")).isEqualTo("(none)");
  }

  @Test
  void handlesMalformedEmail() {
    assertThat(LogMask.email("noatsign")).isEqualTo("***");
    assertThat(LogMask.email("@example.com")).isEqualTo("***");
  }
}
