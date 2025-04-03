package com.ll.quizzle.domain.admin.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.ll.quizzle.domain.admin.dto.request.AdminLoginRequest;
import com.ll.quizzle.global.config.AdminProperties;
import com.ll.quizzle.global.security.oauth2.dto.SecurityUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {
	private final AdminProperties adminProperties;
	private final BCryptPasswordEncoder passwordEncoder;

	public boolean authenticate(AdminLoginRequest request) {
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

		UsernamePasswordAuthenticationToken authentication =
			new UsernamePasswordAuthenticationToken(
				adminUser,
				null,
				adminUser.getAuthorities()
			);

		SecurityContextHolder.getContext().setAuthentication(authentication);

		return true;
	}
}
