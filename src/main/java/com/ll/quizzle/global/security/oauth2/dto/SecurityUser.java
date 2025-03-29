package com.ll.quizzle.global.security.oauth2.dto;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
public class SecurityUser extends User implements OAuth2User {
    private final long id;
    private final String email;
    private final String provider;
    private final boolean isNewUser;
    private final Map<String, Object> attributes;
    private final String oauthId;
    private final String providerAndOauthId;

    // OAuth2 로그인용 생성자
    public SecurityUser(long id, String username, String email, String role,
                        String provider, String oauthId,
                        Map<String, Object> attributes, boolean isNewUser) {
        super(username, "", List.of(new SimpleGrantedAuthority(role)));
        this.id = id;
        this.email = email;
        this.provider = provider;
        this.oauthId = oauthId;
        this.providerAndOauthId = provider + ":" + oauthId;
        this.attributes = attributes != null ? attributes : Collections.emptyMap();
        this.isNewUser = isNewUser;
    }

    public static SecurityUser of(long id, String username, String email, String role,
                                  String provider, String oauthId) {
        return new SecurityUser(id, username, email, role, provider, oauthId, Collections.emptyMap(), false);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return providerAndOauthId;
    }
}
