package com.do_something.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성, 파싱, 검증을 담당하는 컴포넌트.
 * <p>JJWT 0.12.x API를 사용한다.</p>
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-minutes}")
    private int expirationMinutes;

    private SecretKey secretKey;

    @PostConstruct
    private void init() {
        // plain string을 UTF-8 바이트로 변환 — .env에서 32자 이상 설정 필수 (HS256 = 256bit)
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * username을 subject로 갖는 JWT 토큰을 생성한다.
     */
    public String createToken(final String username) {
        final Date now    = new Date();
        final Date expiry = new Date(now.getTime() + (long) expirationMinutes * 60 * 1000);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰에서 username(subject)을 추출한다.
     */
    public String getUsername(final String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 토큰의 서명, 만료 여부를 검증한다.
     *
     * @return 유효하면 {@code true}
     */
    public boolean validateToken(final String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(final String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
