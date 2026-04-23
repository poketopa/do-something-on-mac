package com.do_something.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청 DTO.
 */
public record SignupRequest(
        @NotBlank(message = "username은 필수입니다.")
        @Size(min = 2, max = 50, message = "username은 2~50자 사이여야 합니다.")
        String username,

        @NotBlank(message = "password는 필수입니다.")
        @Size(min = 6, message = "password는 최소 6자 이상이어야 합니다.")
        String password
) {}
