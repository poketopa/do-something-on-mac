package com.do_something.do_something.domain.user;

import com.do_something.do_something.dto.auth.LoginRequest;
import com.do_something.do_something.dto.auth.SignupRequest;
import com.do_something.do_something.dto.auth.SignupResponse;
import com.do_something.do_something.dto.auth.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 REST Controller.
 * <p>Controller는 최대한 얇게 유지한다 — 검증 후 Service 위임만 수행.</p>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** POST /api/users/signup — 회원가입 */
    @PostMapping("/users/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody @Valid final SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.signup(request));
    }

    /** POST /api/token — 로그인 및 JWT 발급 */
    @PostMapping("/token")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid final LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }
}
