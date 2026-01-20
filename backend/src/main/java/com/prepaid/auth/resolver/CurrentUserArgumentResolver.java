package com.prepaid.auth.resolver;

import com.prepaid.auth.annotation.CurrentUser;
import com.prepaid.auth.jwt.JwtProvider;
import com.prepaid.auth.util.CookieUtils;
import com.prepaid.common.exception.specific.UserNotFoundException;
import com.prepaid.domain.User;
import com.prepaid.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @CurrentUser 어노테이션 처리를 위한 ArgumentResolver
 */
@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class) 
                && parameter.getParameterType().equals(User.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        
        // 1. 쿠키에서 accessToken 추출
        String accessToken = CookieUtils.getCookie(request, "accessToken")
                .map(Cookie::getValue)
                .orElseThrow(() -> new RuntimeException("토큰이 없습니다."));
        
        // 2. JWT 인증 정보 추출
        Authentication auth = jwtProvider.getAuthentication(accessToken);
        
        // 3. 사용자 조회 (이메일 기반)
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UserNotFoundException());
    }
}
