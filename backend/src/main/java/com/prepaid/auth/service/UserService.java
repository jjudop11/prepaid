package com.prepaid.auth.service;

import com.prepaid.auth.dto.SignupRequest;
import com.prepaid.auth.validator.PasswordValidator;
import com.prepaid.domain.User;
import com.prepaid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    @Transactional
    public User signup(SignupRequest request) {
        // 1. 아이디 중복 체크
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다");
        }

        // 2. 비밀번호에 아이디 포함 여부 체크
        if (PasswordValidator.containsUsername(request.getPassword(), request.getUsername())) {
            throw new IllegalArgumentException("비밀번호에 아이디가 포함될 수 없습니다");
        }

        // 3. 비밀번호 해시
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 4. User 생성
        User user = User.builder()
                .username(request.getUsername())
                .password(hashedPassword)
                .email(request.getEmail())
                .provider("local")
                .role("USER")
                .failedLoginAttempts(0)
                .accountLocked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    /**
     * 로그인 검증 (비밀번호 확인)
     * @return User if success
     * @throws IllegalStateException if account is locked
     * @throws IllegalArgumentException if auth fails
     */
    @Transactional
    public User authenticateUser(String username, String password) {
        // 1. 사용자 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다"));

        // 2. local 계정만 검증 (OAuth는 여기서 처리 안 함)
        if (!"local".equals(user.getProvider())) {
            throw new IllegalArgumentException("일반 로그인은 local 계정만 가능합니다");
        }

        // 3. 계정 잠금 체크
        if (user.getAccountLocked()) {
            throw new IllegalStateException("계정이 잠겼습니다. 관리자에게 문의하세요");
        }

        // 4. 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            // 실패 시 카운트 증가
            user.incrementFailedAttempts();
            userRepository.save(user);

            int remaining = 5 - user.getFailedLoginAttempts();
            if (remaining <= 0) {
                throw new IllegalStateException("계정이 잠겼습니다. 관리자에게 문의하세요");
            }
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다 (" + user.getFailedLoginAttempts() + "/5)");
        }

        // 5. 성공 시 카운트 초기화
        user.resetFailedAttempts();
        userRepository.save(user);

        return user;
    }

    /**
     * 아이디 중복 체크
     */
    public boolean isUsernameAvailable(String username) {
        return userRepository.findByUsername(username).isEmpty();
    }

    /**
     * 계정 잠금 해제 (관리자용)
     */
    @Transactional
    public void unlockAccount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        
        user.unlock();
        userRepository.save(user);
    }
}
