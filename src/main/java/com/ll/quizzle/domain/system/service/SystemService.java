package com.ll.quizzle.domain.system.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.domain.member.service.AuthTokenService;
import com.ll.quizzle.domain.member.type.Role;
import com.ll.quizzle.domain.system.dto.request.RoleChangeRequest;
import com.ll.quizzle.domain.system.dto.request.SystemLoginRequest;
import com.ll.quizzle.domain.system.dto.response.SystemLoginResponse;
import com.ll.quizzle.domain.system.entity.RoleChangeHistory;
import com.ll.quizzle.domain.system.repository.RoleChangeHistoryRepository;
import com.ll.quizzle.global.config.SystemProperties;
import com.ll.quizzle.global.exceptions.ErrorCode;
import com.ll.quizzle.global.jwt.dto.GeneratedToken;
import com.ll.quizzle.global.jwt.dto.JwtProperties;
import com.ll.quizzle.global.response.RsData;
import com.ll.quizzle.standard.util.CookieUtil;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SystemService {
	private final SystemProperties systemProperties;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	private final AuthTokenService authTokenService;
	private final JwtProperties jwtProperties;
	private final MemberRepository memberRepository;
	private final RoleChangeHistoryRepository roleChangeHistoryRepository;

	public RsData<SystemLoginResponse> authenticate(SystemLoginRequest request, HttpServletResponse response) {
		if (!systemProperties.getSystemEmail().equals(request.systemEmail())) {
			return new RsData<>(HttpStatus.UNAUTHORIZED, "시스템 관리자 이메일이 일치하지 않습니다.", null);
		}

		if (bCryptPasswordEncoder.matches(request.systemPassword(), systemProperties.getSystemPasswordHash())) {
			return new RsData<>(HttpStatus.UNAUTHORIZED, "시스템 관리자 비밀번호가 일치하지 않습니다.", null);
		}

		GeneratedToken tokenInfo = authTokenService.generateToken(request.systemEmail(), "ROLE_SYSTEM");

		addAuthCookies(response, tokenInfo);

		return RsData.success(HttpStatus.OK, new SystemLoginResponse(
			tokenInfo.accessToken(),
			tokenInfo.refreshToken(),
			"ROLE_SYSTEM"
		));
	}

	private void addAuthCookies(HttpServletResponse response, GeneratedToken tokens) {
		CookieUtil.addSystemCookie(
			response,
			"access_token",
			tokens.accessToken(),
			(int) jwtProperties.getAccessTokenExpiration()
		);

		CookieUtil.addSystemCookie(
			response,
			"refresh_token",
			tokens.refreshToken(),
			(int) jwtProperties.getRefreshTokenExpiration()
		);
	}

	public RsData<Void> changeRole(RoleChangeRequest request) {
		Member member = memberRepository.findByEmail(request.targetEmail())
			.orElseThrow(ErrorCode.MEMBER_NOT_FOUND::throwServiceException);

		Role previousRole = member.getRole();

		member.changeRole(request.newRole());

		RoleChangeHistory history = RoleChangeHistory.builder()
			.member(member)
			.previousRole(previousRole)
			.newRole(request.newRole())
			.reason(request.reason())
			.changedBy(systemProperties.getSystemEmail())
			.build();

		roleChangeHistoryRepository.save(history);

		return new RsData<>(HttpStatus.OK, "권한이 변경되었습니다.", null);
	}
}
