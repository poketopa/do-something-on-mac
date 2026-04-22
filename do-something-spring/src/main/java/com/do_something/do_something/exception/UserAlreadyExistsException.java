package com.do_something.do_something.exception;

/**
 * 이미 존재하는 username으로 회원가입 시도할 때 던지는 예외.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(final String username) {
        super("이미 사용 중인 username입니다: " + username);
    }
}
