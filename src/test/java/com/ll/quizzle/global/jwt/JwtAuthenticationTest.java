package com.ll.quizzle.global.jwt;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.domain.member.service.AuthTokenService;
import com.ll.quizzle.domain.member.service.MemberService;
import com.ll.quizzle.domain.member.type.Role;
import com.ll.quizzle.global.exceptions.ServiceException;
import com.ll.quizzle.global.jwt.dto.GeneratedToken;
import com.ll.quizzle.global.jwt.dto.JwtProperties;
import com.ll.quizzle.global.request.Rq;
import com.ll.quizzle.global.response.RsData;
import com.ll.quizzle.global.security.oauth2.entity.OAuth;
import com.ll.quizzle.global.security.oauth2.repository.OAuthRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static com.ll.quizzle.global.exceptions.ErrorCode.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class JwtAuthenticationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OAuthRepository oAuthRepository;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Mock
    private Rq rq;

    private Member testMember;
    private OAuth testOAuth;
    private GeneratedToken generatedTokens;
    private final String testProvider = "google";
    private final String testOauthId = "12345";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // MockMvc 설정
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilter(jwtAuthFilter)
                .apply(springSecurity())
                .alwaysDo(print())
                .build();

        // 테스트 멤버 생성
        testMember = Member.builder()
                .nickname("OAuth2 테스트")
                .email("oauth2test@example.com")
                .level(0)
                .role(Role.MEMBER)
                .exp(0)
                .profilePath("test")
                .pointBalance(0)
                .build();

        // 테스트용 OAuth 정보 생성
        testOAuth = OAuth.builder()
                .provider(testProvider)
                .oauthId(testOauthId)
                .member(testMember)
                .build();

        memberRepository.save(testMember);
        oAuthRepository.save(testOAuth);

        // 토큰 생성
        generatedTokens = authTokenService.generateToken(
                testProvider,
                testOauthId,
                testMember.getRole().toString()
        );

        ReflectionTestUtils.setField(memberService, "rq", rq);

        when(rq.getActor()).thenReturn(testMember);

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("쿠키에 액세스 토큰을 담아 API 호출")
    void accessAPI_WithCookieToken() throws Exception {
        // given
        Cookie accessTokenCookie = new Cookie("access_token", generatedTokens.accessToken());
        accessTokenCookie.setPath("/");
        accessTokenCookie.setHttpOnly(true);

        // when & then
        // todo : api 추가된 후 url 수정 필요
        mockMvc.perform(delete("/api/v1/members")
                        .cookie(accessTokenCookie))
                .andExpect(status().is(204));
    }

    @Test
    @DisplayName("토큰 없이 API 호출 시 401 에러")
    void noToken_Unauthorized() throws Exception {
        // 인증 실패 케이스 테스트
        when(rq.getActor()).thenThrow(new ServiceException(UNAUTHORIZED.getHttpStatus(), "로그인이 필요합니다."));

        // when & then
        mockMvc.perform(get("/api/v1/members/1/points"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("리프레시 토큰으로 액세스 토큰 갱신 테스트")
    void refreshToken_Success() {
        // given
        String refreshToken = generatedTokens.refreshToken();

        // when
        RsData<String> result = memberService.refreshAccessToken(refreshToken);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResultCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getData()).isNotEmpty();

        // 새 토큰으로 사용자 식별자 추출 테스트
        String providerAndOauthId = memberService.getProviderAndOauthIdFromToken(result.getData());
        assertThat(providerAndOauthId).isEqualTo(testProvider + ":" + testOauthId);
    }

    @Test
    @DisplayName("만료된 토큰 검증 테스트")
    void expiredToken_FailsValidation() {
        // given
        String expiredToken = generateExpiredToken();

        // when
        boolean isValid = memberService.verifyToken(expiredToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("JWT 토큰 검증 성공 테스트")
    void validateToken_Success() {
        // given
        String token = generatedTokens.accessToken();

        // when
        boolean isValid = memberService.verifyToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("토큰에서 사용자 식별자 추출 테스트")
    void extractEmailFromToken() {
        // given
        String token = generatedTokens.accessToken();

        // when
        String providerAndOauthId = memberService.getProviderAndOauthIdFromToken(token);

        // then
        assertThat(providerAndOauthId).isEqualTo(testProvider + ":" + testOauthId);
    }

    // 만료된 토큰 생성 도우미 메서드
    private String generateExpiredToken() {
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis - 1000; // 1초 전에 만료됨

        return Jwts.builder()
                .setSubject(testMember.getEmail())
                .claim("role", testMember.getRole())
                .claim("type", "access")
                .setIssuedAt(new Date(nowMillis))
                .setExpiration(new Date(expMillis))
                .signWith(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes()))
                .compact();
    }
} 