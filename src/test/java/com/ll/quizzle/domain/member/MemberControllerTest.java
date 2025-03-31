package com.ll.quizzle.domain.member;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.factory.TestMemberFactory;
import com.ll.quizzle.global.security.oauth2.repository.OAuthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private Member member;

    @BeforeEach
    void setUp() {
        // 테스트 유저 생성
        member = TestMemberFactory.createOAuthMember(
                "테스트유저", "test@email.com", "google", "1234",
                memberRepository, oAuthRepository
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
}
