package com.ll.quizzle.domain.admin.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.ll.quizzle.domain.admin.dto.request.AdminLoginRequest;
import com.ll.quizzle.global.exceptions.ErrorCode;
import com.ll.quizzle.global.security.oauth2.dto.SecurityUser;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {
	private final AdminProperties adminProperties;
	private final BCryptPasswordEncoder passwordEncoder;

	public static final String SESSION_KEY_ADMIN = "ADMIN_KEY";

	public boolean authenticate(AdminLoginRequest request, HttpSession session) {
		if (!adminProperties.getAdminEmail().equals(request.adminEmail())) {
			return false;
		}

		if (!passwordEncoder.matches(request.adminPassword(), adminProperties.getAdminPasswordHash())) {
			return false;
		}

		SecurityUser adminUser = SecurityUser.of(
			0L,
			"관리자",           // 표시될 닉네임
			adminProperties.getAdminEmail(),    // .env에서 가져온 이메일
			"ROLE_ADMIN"
		);

		// 세션에 관리자 정보 저장
		session.setAttribute(SESSION_KEY_ADMIN, adminProperties.getAdminEmail());
		session.setAttribute("ADMIN_ROLE", "ROLE_ADMIN");

		UsernamePasswordAuthenticationToken authentication =
			new UsernamePasswordAuthenticationToken(
				adminUser,
				null,
				adminUser.getAuthorities()
			);

		SecurityContextHolder.getContext().setAuthentication(authentication);

		return true;
	}

	public void logout(HttpSession session) {
		String adminEmail = (String) session.getAttribute(SESSION_KEY_ADMIN);

		// 관리자 세션이 아닌 경우 예외 처리
		if (adminEmail == null || !adminProperties.getAdminEmail().equals(adminEmail)) {
			throw ErrorCode.UNAUTHORIZED.throwServiceException();
		}

		session.invalidate();
		SecurityContextHolder.clearContext();
	}
}
