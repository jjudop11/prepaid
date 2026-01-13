package com.prepaid.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    @NotBlank(message = "아이디를 입력하세요")
    @Pattern(regexp = "^[a-z0-9]{4,20}$", message = "아이디는 영문 소문자와 숫자 4-20자여야 합니다")
    private String username;

    @NotBlank(message = "비밀번호를 입력하세요")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]).+$",
        message = "비밀번호는 영문자, 숫자, 특수기호를 모두 포함해야 합니다"
    )
    private String password;

    @Email(message = "유효한 이메일 주소를 입력하세요")
    private String email; // 선택사항
}
