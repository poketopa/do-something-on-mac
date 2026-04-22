package com.do_something.do_something.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 DTO.
 * <p>기존 Python 코드의 OAuth2 form-data 방식에서 JSON Body 방식으로 변경.</p>
 */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {}
