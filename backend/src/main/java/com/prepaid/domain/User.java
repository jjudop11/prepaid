package com.prepaid.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true) // nullable for OAuth users
    private String username;

    private String password; // BCrypt hashed, nullable for OAuth users

    @Column(nullable = false)
    private String provider; // "local" or "naver"

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private String role = "USER";

    // 계정 잠금 관련 필드 (local 계정만 사용)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Builder.Default
    private Boolean accountLocked = false;

    private LocalDateTime lockedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 비즈니스 로직 메서드
    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.accountLocked = true;
            this.lockedAt = LocalDateTime.now();
        }
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
    }

    public void unlock() {
        this.accountLocked = false;
        this.failedLoginAttempts = 0;
        this.lockedAt = null;
    }
}
