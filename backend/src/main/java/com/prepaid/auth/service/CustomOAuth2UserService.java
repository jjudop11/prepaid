package com.prepaid.auth.service;

import com.prepaid.domain.User;
import com.prepaid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "naver"
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName(); // "response"

        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Naver returns response inside "response" key
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        String email = (String) response.get("email");
        String provider = registrationId.toUpperCase();

        User user = saveOrUpdateUser(email, provider);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole())),
                attributes,
                userNameAttributeName);
    }

    private User saveOrUpdateUser(String email, String provider) {
        User user = userRepository.findByProviderAndEmail(provider, email)
                .orElse(User.builder()
                        .email(email)
                        .provider(provider)
                        .role("ROLE_USER")
                        .createdAt(LocalDateTime.now())
                        .build());

        // Update basic info if needed (not much mutable here for now)
        return userRepository.save(user);
    }
}
