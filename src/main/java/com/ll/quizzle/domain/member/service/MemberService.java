package com.ll.quizzle.domain.member.service;

import static com.ll.quizzle.global.exceptions.ErrorCode.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.quizzle.domain.avatar.entity.Avatar;
import com.ll.quizzle.domain.avatar.repository.AvatarRepository;
import com.ll.quizzle.domain.member.dto.response.MemberProfileEditResponse;
import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.domain.point.service.PointService;
import com.ll.quizzle.domain.point.type.PointReason;
import com.ll.quizzle.global.jwt.dto.GeneratedToken;
import com.ll.quizzle.global.jwt.dto.JwtProperties;
import com.ll.quizzle.global.request.Rq;
import com.ll.quizzle.global.response.RsData;
import com.ll.quizzle.global.security.oauth2.repository.OAuthRepository;
import com.ll.quizzle.standard.util.CookieUtil;
import com.ll.quizzle.standard.util.Ut;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;
	private final AvatarRepository avatarRepository;
	private final PointService pointService;
	private final OAuthRepository oAuthRepository;
	private final RefreshTokenService refreshTokenService;
	private final RedisTemplate<String, String> redisTemplate;
	private final AuthTokenService authTokenService;
	private final JwtProperties jwtProperties;
	private final Rq rq;

	private static final String LOGOUT_PREFIX = "LOGOUT:";

	@Transactional(readOnly = true)
	public Member findByProviderAndOauthId(String provider, String oauthId) {
		return oAuthRepository.findByProviderAndOauthIdWithMember(provider, oauthId)
			.orElseThrow(OAUTH_NOT_FOUND::throwServiceException)
			.getMember();
	}

	public Optional<Member> findById(Long id) {
		return memberRepository.findById(id);
	}

	public Optional<Member> findByEmail(String email) {
		return memberRepository.findByEmail(email);
	}

	public String generateRefreshToken(String email) {
		return refreshTokenService.generateRefreshToken(email);
	}

	public String extractEmailIfValid(String token) {
		if (isLoggedOut(token)) {
			TOKEN_LOGGED_OUT.throwServiceException();
		}
		if (!verifyToken(token)) {
			TOKEN_INVALID.throwServiceException();
		}
		return getEmailFromToken(token);
	}

	public boolean isLoggedOut(String token) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(LOGOUT_PREFIX + token));
	}

	public boolean verifyToken(String accessToken) {
		return authTokenService.verifyToken(accessToken);
	}

	public String getEmailFromToken(String token) {
		return authTokenService.getEmail(token);
	}

	public Long getTokenExpiryTime(String token) {
		return authTokenService.getTokenExpiryTime(token);
	}

	public RsData<String> refreshAccessToken(String refreshToken) {
		return refreshTokenService.refreshAccessToken(refreshToken);
	}

	@Transactional
	public void oAuth2Login(Member member, HttpServletResponse response) {
		// 기본 아바타 없으면 할당
		if (member.getAvatar() == null) {

			Avatar defaultAvatar = avatarRepository.findAll().stream()
				.filter(a -> a.getFileName().trim().equalsIgnoreCase("새콩이"))
				.findFirst()
				.orElseThrow(AVATAR_NOT_FOUND::throwServiceException);

			member.changeAvatar(defaultAvatar);
			memberRepository.save(member);
		}

		GeneratedToken tokens = authTokenService.generateToken(
			member.getEmail(),
			member.getUserRole()
		);

		addAuthCookies(response, tokens, member);
	}

	private void addAuthCookies(HttpServletResponse response, GeneratedToken tokens, Member member) {
		// Access Token 쿠키
		CookieUtil.addCookie(
			response,
			"access_token",
			tokens.accessToken(),
			(int)jwtProperties.getAccessTokenExpiration(),
			true,
			true
		);

		// Refresh Token 쿠키
		CookieUtil.addCookie(
			response,
			"refresh_token",
			tokens.refreshToken(),
			(int)jwtProperties.getRefreshTokenExpiration(),
			true,
			true
		);

		// Role 쿠키
		Map<String, Object> roleData = new HashMap<>();
		roleData.put("role", member.getUserRole());

		CookieUtil.addCookie(
			response,
			"role",
			URLEncoder.encode(Ut.json.toString(roleData), StandardCharsets.UTF_8),
			(int)jwtProperties.getAccessTokenExpiration(),
			false,
			true
		);
	}

	public void logout(HttpServletRequest request, HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();
		String accessToken = null;
		String refreshToken = null;

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("access_token".equals(cookie.getName())) {
					accessToken = cookie.getValue();
				} else if ("refresh_token".equals(cookie.getName())) {
					refreshToken = cookie.getValue();
				}
			}
		}

		// 액세스 토큰이 만료되었다면 리프레시 토큰으로 처리
		if (accessToken == null && refreshToken != null) {
			RsData<String> refreshResult = refreshAccessToken(refreshToken);
			if (refreshResult.isSuccess()) {
				accessToken = refreshResult.getData();
			}
		}

		if (accessToken == null) {
			UNAUTHORIZED.throwServiceException();
		}

		String email = authTokenService.getEmail(accessToken);

		// Redis에서 토큰 무효화
		redisTemplate.opsForValue().set(
			LOGOUT_PREFIX + accessToken,
			email,
			jwtProperties.getAccessTokenExpiration(),
			TimeUnit.MILLISECONDS
		);

		// Refresh 토큰 삭제
		refreshTokenService.removeRefreshToken(email);

		CookieUtil.deleteCookie(request, response, "access_token");
		CookieUtil.deleteCookie(request, response, "refresh_token");
		CookieUtil.deleteCookie(request, response, "role");
		CookieUtil.deleteCookie(request, response, "oauth2_auth_request");
	}

	@Transactional
	public MemberProfileEditResponse editNickname(Long memberId, String newNickname) {
		Member member = rq.assertIsOwner(memberId);

		validateNickname(newNickname);
		member.changeNickname(newNickname);
		pointService.applyPointPolicy(member, PointReason.NICKNAME_CHANGE);
		memberRepository.save(member);
		return MemberProfileEditResponse.from(member);
	}

	@Transactional
	public void editAvatar(Long memberId, Long avatarId) {
		Member member = rq.assertIsOwner(memberId);

		Avatar avatar = avatarRepository.findById(avatarId)
			.orElseThrow(AVATAR_NOT_FOUND::throwServiceException);

		if (!avatar.isOwned() || !avatar.getMember().getId().equals(memberId)) {
			throw AVATAR_NOT_OWNED.throwServiceException();
		}

		if (member.getAvatar() != null && member.getAvatar().getId().equals(avatarId)) {
			throw AVATAR_ALREADY_APPLIED.throwServiceException();
		}

		member.changeAvatar(avatar);
		memberRepository.save(member);
	}

	public void validateNickname(String nickname) {
		if (nickname == null || nickname.trim().isEmpty()) {
			NICKNAME_INVALID.throwServiceException();
		}
		if (nickname.length() < 2 || nickname.length() > 20) {
			NICKNAME_LENGTH_INVALID.throwServiceException();
		}
		if (!nickname.matches("^[a-zA-Z0-9가-힣]+$")) {
			NICKNAME_FORMAT_INVALID.throwServiceException();
		}
		if (memberRepository.existsByNickname(nickname)) {
			NICKNAME_ALREADY_EXISTS.throwServiceException();
		}
	}
}
