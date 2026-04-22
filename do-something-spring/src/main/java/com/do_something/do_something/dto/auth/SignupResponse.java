package com.do_something.do_something.dto.auth;

import com.do_something.do_something.domain.user.User;

/**
 * 회원가입 응답 DTO.
 */
public record SignupResponse(Long id, String username) {

    public static SignupResponse from(final User user) {
        return new SignupResponse(user.getId(), user.getUsername());
    }
}
