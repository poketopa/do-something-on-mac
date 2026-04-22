package com.do_something.do_something.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리기.
 * <p>모든 Controller에서 발생하는 예외를 한 곳에서 처리해 일관된 에러 응답 형식을 보장한다.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 400 — 이미 존재하는 username */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(final UserAlreadyExistsException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }

    /** 404 — 사용자를 찾을 수 없음 */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(final UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
    }

    /** 401 — 아이디 또는 비밀번호 불일치 */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(final BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("아이디 또는 비밀번호가 올바르지 않습니다."));
    }

    /** 422 — @Valid 유효성 검사 실패 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(final MethodArgumentNotValidException e) {
        final Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
        return ResponseEntity.status(422).body(errors);
    }

    /** 500 — 나머지 예외 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(final Exception e) {
        return ResponseEntity.internalServerError().body(new ErrorResponse("서버 오류가 발생했습니다."));
    }

    /** 에러 응답 공통 포맷 */
    public record ErrorResponse(String detail) {}
}
