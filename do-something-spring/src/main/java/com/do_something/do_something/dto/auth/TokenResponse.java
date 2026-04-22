package com.do_something.do_something.dto.auth;

/**
 * JWT 토큰 발급 응답 DTO.
 */
public record TokenResponse(String accessToken, String tokenType) {

    public static TokenResponse bearer(final String accessToken) {
        return new TokenResponse(accessToken, "bearer");
    }
}
