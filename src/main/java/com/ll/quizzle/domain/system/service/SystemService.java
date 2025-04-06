package com.ll.quizzle.domain.system.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.domain.member.service.AuthTokenService;
import com.ll.quizzle.domain.member.service.RefreshTokenService;
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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SystemService {
	private final SystemProperties systemProperties;
	private final BCryptPasswordEncoder passwordEncoder;
	private final AuthTokenService authTokenService;
	private final JwtProperties jwtProperties;
	private final MemberRepository memberRepository;
	private final RoleChangeHistoryRepository roleChangeHistoryRepository;
	private final RefreshTokenService refreshTokenService;

	public RsData<SystemLoginResponse> authenticate(SystemLoginRequest request, HttpServletResponse response) {
		log.info("시스템 관리자 로그인 시도 - email: {}", request.systemEmail());

		try {
			if (!systemProperties.getSystemEmail().equals(request.systemEmail())) {
				log.warn("시스템 관리자 로그인 실패 - 잘못된 이메일: {}", request.systemEmail());
				return new RsData<>(HttpStatus.UNAUTHORIZED, "시스템 관리자 이메일이 일치하지 않습니다.", null);
			}

			if (!passwordEncoder.matches(request.systemPassword(), systemProperties.getSystemPasswordHash())) {
				log.warn("시스템 관리자 로그인 실패 - 잘못된 비밀번호: {}", request.systemPassword());
				return new RsData<>(HttpStatus.UNAUTHORIZED, "시스템 관리자 비밀번호가 일치하지 않습니다.", null);
			}

			GeneratedToken tokenInfo = authTokenService.generateToken(request.systemEmail(), "ROLE_SYSTEM");
			log.debug("토근 생성 완료: {}", tokenInfo);

			addAuthCookies(response, tokenInfo);

			return RsData.success(HttpStatus.OK, new SystemLoginResponse(
				tokenInfo.accessToken(),
				tokenInfo.refreshToken(),
				"ROLE_SYSTEM"
			));

		} catch (Exception e) {
			log.error("관리자 로그인 중 오류 발생: {}", e.getMessage());
			throw e;
		}
	}

	private void addAuthCookies(HttpServletResponse response, GeneratedToken tokens) {

		try {
			log.debug("엑세스 토큰 쿠키 발급");
			CookieUtil.addSystemCookie(
				response,
				"access_token",
				tokens.accessToken(),
				(int)jwtProperties.getAccessTokenExpiration()
			);

			log.debug("리프레시 토큰 쿠키 발급");
			CookieUtil.addSystemCookie(
				response,
				"refresh_token",
				tokens.refreshToken(),
				(int)jwtProperties.getRefreshTokenExpiration()
			);

		} catch (Exception e) {
			log.error("쿠키 설정 중 오류 발생: {}", e.getMessage());
		}
	}

	@Transactional
	public RsData<Void> changeRole(RoleChangeRequest request) {
		log.info("권한 변경 시도 - 대상: {}, 새 권한: {}, 요청자: {}",
			request.targetEmail(),
			request.newRole(),
			systemProperties.getSystemEmail());

		try {
			Member member = memberRepository.findByEmail(request.targetEmail())
				.orElseThrow(() -> {
					log.warn("권한 변경 실패 - 사용자를 찾을 수 없음: {}", request.targetEmail());
					return ErrorCode.MEMBER_NOT_FOUND.throwServiceException();
				});

			Role previousRole = member.getRole();
			log.debug("현재 권한 확인 - email: {}, role: {}", request.targetEmail(), previousRole);

			member.changeRole(request.newRole());
			log.debug("권한 변경 완료 - email: {}, role: {}", request.targetEmail(), request.newRole());

			RoleChangeHistory history = RoleChangeHistory.builder()
				.member(member)
				.previousRole(previousRole)
				.newRole(request.newRole())
				.reason(request.reason())
				.changedBy(systemProperties.getSystemEmail())
				.build();

			roleChangeHistoryRepository.save(history);
			log.debug("권한 변경 이력 저장 완료 - historyId: {}", history.getId());

			log.info("권한 변경 성공 - 대상: {}, 이전 권한: {}, 새 권한: {}",
				request.targetEmail(),
				previousRole,
				request.newRole());

			return new RsData<>(HttpStatus.OK, "권한이 변경되었습니다.", null);

		} catch (Exception e) {
			log.error("권한 변경 중 오류 발생 - 대상: {}, 의도한 권한: {}, 오류: {}",
				request.targetEmail(),
				request.newRole(),
				e.getMessage());
			throw e;
		}
	}

	@Transactional
	public RsData<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		log.info("시스템 관리자 로그아웃 시도");

		try {
			String refreshToken = null;
			Cookie[] cookies = request.getCookies();

			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("refresh_token".equals(cookie.getName())) {
						refreshToken = cookie.getValue();
						break;
					}
				}
			}

			if (refreshToken != null) {
				log.debug("리프레시 토큰 삭제 시도");
				refreshTokenService.removeRefreshToken(refreshToken);
				log.debug("리프레시 토큰 삭제 완료");
			} else {
				log.warn("로그아웃 시 리프레시 토큰을 찾을 수 없음");
			}

			log.debug("인증 쿠키 삭제 시작");
			CookieUtil.deleteCookie(request, response, "access_token");
			CookieUtil.deleteCookie(request, response, "refresh_token");
			log.debug("인증 쿠키 삭제 완료");

			log.info("시스템 관리자 로그아웃 성공");
			return new RsData<>(HttpStatus.OK, "로그아웃 되었습니다.", null);

		} catch (Exception e) {
			log.error("로그아웃 처리 중 오류 발생: {}", e.getMessage());
			throw e;
		}
	}
}
