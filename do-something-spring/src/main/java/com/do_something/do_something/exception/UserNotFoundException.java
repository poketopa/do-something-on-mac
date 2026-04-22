package com.do_something.do_something.exception;

/**
 * 존재하지 않는 사용자를 조회할 때 던지는 예외.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(final String username) {
        super("사용자를 찾을 수 없습니다: " + username);
    }
}
