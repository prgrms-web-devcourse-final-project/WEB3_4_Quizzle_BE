package com.ll.quizzle.global.security.oauth2;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.domain.member.service.MemberService;
import com.ll.quizzle.domain.member.type.Role;
import com.ll.quizzle.global.request.Rq;
import com.ll.quizzle.global.security.oauth2.entity.OAuth;
import com.ll.quizzle.global.security.oauth2.repository.OAuthRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
public class OAuth2AuthenticationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OAuthRepository oAuthRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private Rq rq;

    private Member testMember;
    private OAuth testOAuth;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        response = new MockHttpServletResponse();

        // Rq 모킹 설정
        ReflectionTestUtils.setField(memberService, "rq", rq);

        // 테스트용 회원 생성
        testMember = Member.builder()
                .nickname("OAuth2 테스트")
                .email("oauth2test@example.com")
                .level(0)
                .role(Role.MEMBER)
                .exp(0)
                .profilePath("test")
                .pointBalance(0)
                .oauths(new ArrayList<>())
                .build();

        memberRepository.save(testMember);

        // 테스트용 OAuth 정보 생성
        testOAuth = OAuth.builder()
                .provider("google")
                .oauthId("123456789")
                .member(testMember)
                .build();

        testMember.getOauths().add(testOAuth);
        oAuthRepository.save(testOAuth);
        memberRepository.save(testMember);

        when(rq.getActor()).thenReturn(testMember);
    }

    @Test
    @DisplayName("OAuth2 로그인 후 토큰 검증")
    void validateTokenAfterOAuth2Login() {
        // given
        // OAuth2 로그인 프로세스
        memberService.oAuth2Login(testMember, response);

        // 토큰 추출
        String accessToken = response.getCookie("access_token").getValue();
        String refreshToken = response.getCookie("refresh_token").getValue();

        // when & then
        // 액세스 토큰 검증
        assertThat(memberService.verifyToken(accessToken)).isTrue();
        // 리프레시 토큰 검증
        assertThat(memberService.verifyToken(refreshToken)).isTrue();
    }

    @Test
    @DisplayName("다른 OAuth 제공자 계정 연결 성공")
    void linkAnotherOAuthProvider_Success() {
        // given
        String provider = "kakao";
        String oauthId = "kakao12345";

        // when
        OAuth linkedOAuth = OAuth.builder()
                .provider(provider)
                .oauthId(oauthId)
                .member(testMember)
                .build();

        testMember.getOauths().add(linkedOAuth);
        oAuthRepository.save(linkedOAuth);
        memberRepository.save(testMember);

        // then
        Member updatedMember = memberRepository.findById(testMember.getId()).orElseThrow();
        assertThat(updatedMember.getOauths()).hasSize(2);
        assertThat(updatedMember.getOauths()).anyMatch(oauth ->
                oauth.getProvider().equals(provider) && oauth.getOauthId().equals(oauthId)
        );
    }

    @Test
    @DisplayName("OAuth 쿠키 기반 인증 테스트")
    void cookieBasedAuthenticationTest() throws Exception {
        // given
        memberService.oAuth2Login(testMember, response);
        Cookie accessTokenCookie = response.getCookie("access_token");

        // when & then
        // todo : api 추가된 후 url 수정 필요
        mockMvc.perform(delete("/api/v1/members")
                        .cookie(accessTokenCookie))
                .andDo(print())
                .andExpect(status().is(204));
    }

    @Test
    @DisplayName("OAuth 인증 후 SecurityContext 설정 테스트")
    void securityContextAfterOAuth2Login() {
        // given
        // 인증 정보 생성
        Authentication auth = new UsernamePasswordAuthenticationToken(
                testMember,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_MEMBER"))
        );

        // when
        SecurityContextHolder.getContext().setAuthentication(auth);

        // then
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(currentAuth).isNotNull();
        assertThat(currentAuth.getPrincipal()).isEqualTo(testMember);
    }

    @Test
    @DisplayName("OAuth 제공자로 회원 조회 테스트")
    void findMemberByProviderAndOauthId() {
        // given
        String provider = "google";
        String oauthId = "123456789";

        // when
        Member foundMember = memberService.findByProviderAndOauthId(provider, oauthId);

        // then
        assertThat(foundMember).isNotNull();
        assertThat(foundMember.getId()).isEqualTo(testMember.getId());
        assertThat(foundMember.getEmail()).isEqualTo(testMember.getEmail());
    }

    @Test
    @DisplayName("OAuth 로그인 실패 - 유효하지 않은 제공자")
    void oauthLoginFail_InvalidProvider() {
        // given
        String invalidProvider = "invalid-provider";
        String oauthId = "123456789";

        // when & then
        assertThat(oAuthRepository.findByProviderAndOauthIdWithMember(invalidProvider, oauthId))
                .isEmpty();
    }
}