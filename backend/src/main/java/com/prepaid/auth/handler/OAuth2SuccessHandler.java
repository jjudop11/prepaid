package com.prepaid.auth.handler;

import com.prepaid.auth.jwt.JwtProvider;
import com.prepaid.auth.util.CookieUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;

    @Value("${jwt.expiration}")
    private long accessTokenExpiration; // ms

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration; // ms

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        // 1. Create Tokens
        String accessToken = jwtProvider.createAccessToken(authentication);
        String refreshToken = jwtProvider.createRefreshToken(authentication);

        log.info("OAuth2 Login Success. AccessToken: {}, RefreshToken: {}", accessToken, refreshToken);

        // 2. Set Cookies (HttpOnly)
        // Convert ms to seconds for MaxAge
        CookieUtils.addCookie(response, "accessToken", accessToken, (int) (accessTokenExpiration / 1000));
        CookieUtils.addCookie(response, "refreshToken", refreshToken, (int) (refreshTokenExpiration / 1000));

        // 3. Redirect to Frontend Home
        getRedirectStrategy().sendRedirect(request, response, "http://localhost:3000/");
    }
}
