package com.prepaid.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UserDto user;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserDto {
        private Long id;
        private String username;
        private String email;
        private String role;
    }
}
