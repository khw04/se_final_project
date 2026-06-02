package com.pokemo.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @Email(message = "올바른 이메일 형식이 아닙니다") @NotBlank(message = "이메일을 입력하세요") String email,
    @NotBlank(message = "비밀번호를 입력하세요")
    @Size(min = 8, max = 72, message = "비밀번호는 8자 이상 72자 이하여야 합니다") String password
) {
}
