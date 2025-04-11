package com.ll.quizzle.domain.member;

import static com.ll.quizzle.global.exceptions.ErrorCode.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.ll.quizzle.domain.avatar.entity.Avatar;
import com.ll.quizzle.domain.avatar.repository.AvatarRepository;
import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.factory.TestMemberFactory;
import com.ll.quizzle.global.security.oauth2.repository.OAuthRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OAuthRepository oAuthRepository;

    @Autowired
    private AvatarRepository avatarRepository;

    private Member member;

    @BeforeEach
    void setUp() {

        Avatar defaultAvatar = avatarRepository.findByFileName("새콩이")
            .orElseThrow(AVATAR_NOT_FOUND::throwServiceException);
        // 테스트 유저 생성
        member = TestMemberFactory.createOAuthMember(
                "테스트유저", "test@email.com", "google", "1234",
                memberRepository, oAuthRepository, defaultAvatar
        );
    }

    @Test
    @DisplayName("프로필 정보 조회 (인증 필요 없음)")
    void testGetProfile() throws Exception {
        mockMvc.perform(get("/api/v1/members/{memberId}", member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(member.getId()))
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()));
    }

    @Test
    @DisplayName("프로필 정보 조회 실패 - 없는 회원 (99999999)")
    void testGetProfileWithNoMember() throws Exception {
        mockMvc.perform(get("/api/v1/members/99999999"))
                .andExpect(status().isNotFound());
    }
}
