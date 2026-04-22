package com.do_something.do_something.domain.user;

import com.do_something.do_something.dto.auth.LoginRequest;
import com.do_something.do_something.dto.auth.SignupRequest;
import com.do_something.do_something.dto.auth.SignupResponse;
import com.do_something.do_something.dto.auth.TokenResponse;
import com.do_something.do_something.exception.UserAlreadyExistsException;
import com.do_something.do_something.exception.UserNotFoundException;
import com.do_something.do_something.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 인증 관련 비즈니스 로직.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입.
     *
     * @throws UserAlreadyExistsException username이 이미 존재할 때
     */
    @Transactional
    public SignupResponse signup(final SignupRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException(request.username());
        }

        final String encodedPassword = passwordEncoder.encode(request.password());
        final User user = User.create(request.username(), encodedPassword);
        return SignupResponse.from(userRepository.save(user));
    }

    /**
     * 로그인 후 JWT 토큰 발급.
     *
     * @throws UserNotFoundException     사용자가 없을 때
     * @throws BadCredentialsException   비밀번호 불일치 시
     */
    public TokenResponse login(final LoginRequest request) {
        final User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UserNotFoundException(request.username()));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 올바르지 않습니다.");
        }

        final String token = jwtTokenProvider.createToken(user.getUsername());
        return TokenResponse.bearer(token);
    }
}
