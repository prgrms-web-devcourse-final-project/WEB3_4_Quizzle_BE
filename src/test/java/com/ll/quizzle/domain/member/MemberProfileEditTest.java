package com.ll.quizzle.domain.member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.domain.member.service.AuthTokenService;
import com.ll.quizzle.factory.TestMemberFactory;
import com.ll.quizzle.global.jwt.dto.GeneratedToken;
import com.ll.quizzle.global.security.oauth2.repository.OAuthRepository;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberProfileEditTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private OAuthRepository oauthRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Member member;
    private Cookie accessTokenCookie;

    @BeforeEach
    void setUp() {
        member = TestMemberFactory.createOAuthMember("테스트유저", "test@email.com", "google", "1234", memberRepository, oauthRepository);
        GeneratedToken token = authTokenService.generateToken(member.getEmail(), member.getRole().name());
        accessTokenCookie = new Cookie("access_token", token.accessToken());
    }

    @Test
    @DisplayName("닉네임이 공백일 때")
    void updateNickname_empty() throws Exception {
        mockMvc.perform(patch("/api/v1/members/" + member.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(accessTokenCookie)
                .content(objectMapper.writeValueAsString(Map.of("nickname", " "))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value(org.hamcrest.Matchers.containsString("닉네임은 2자 이상 20자 이하로 입력해주세요.")));
    }

    @Test
    @DisplayName("닉네임이 너무 짧을 때")
    void updateNickname_tooShort() throws Exception {
        mockMvc.perform(patch("/api/v1/members/" + member.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(accessTokenCookie)
                .content(objectMapper.writeValueAsString(Map.of("nickname", "A"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value(org.hamcrest.Matchers.containsString("닉네임은 2자 이상 20자 이하로 입력해주세요.")));
    }

    @Test
    @DisplayName("닉네임에 특수문자가 포함될 때")
    void updateNickname_invalidFormat() throws Exception {
        mockMvc.perform(patch("/api/v1/members/" + member.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(accessTokenCookie)
                .content(objectMapper.writeValueAsString(Map.of("nickname", "Invalid@Nick"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("닉네임은 영문, 숫자, 한글만 사용할 수 있습니다."));
    }

    @Test
    @DisplayName("중복된 닉네임일 때")
    void updateNickname_duplicate() throws Exception {
        TestMemberFactory.createOAuthMember("중복닉네임", "duplicate@email.com", "google", "5678", memberRepository, oauthRepository);
        mockMvc.perform(patch("/api/v1/members/" + member.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(accessTokenCookie)
                .content(objectMapper.writeValueAsString(Map.of("nickname", "중복닉네임"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("이미 존재하는 닉네임입니다."));
    }
}