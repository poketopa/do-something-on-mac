package com.do_something.domain.user;

import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자 계정 Entity.
 * <p>JPA Entity이므로 @NoArgsConstructor(PROTECTED)로 직접 생성 방지.
 * 생성은 반드시 {@link User#create(String, String)} 팩토리 메서드를 사용한다.</p>
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username"})
@EqualsAndHashCode(of = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    /**
     * 사용자 생성 팩토리 메서드.
     *
     * @param username 사용자 ID
     * @param encodedPassword BCrypt 인코딩된 비밀번호
     */
    public static User create(final String username, final String encodedPassword) {
        final User user = new User();
        user.username = username;
        user.password = encodedPassword;
        return user;
    }
}
