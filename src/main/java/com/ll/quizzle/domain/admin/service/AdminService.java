package com.ll.quizzle.domain.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.ll.quizzle.domain.admin.dto.AdminLoginRequestDTO;
import com.ll.quizzle.global.security.oauth2.dto.SecurityUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {
	@Value("${admin.email}")
	private String adminEmail;

	@Value("${admin.password-hash}")
	private String adminPasswordHash;

	private final BCryptPasswordEncoder passwordEncoder;

	public boolean authenticate(AdminLoginRequestDTO request) {
		if (!adminEmail.equals(request.adminEmail())) {
			return false;
		}

		if (!adminPasswordHash.equals(request.adminPasswordHash())) {
			return false;
		}

		SecurityUser adminUser = SecurityUser.of(
			0L,
			"관리자",           // 표시될 닉네임
			adminEmail,    // .env에서 가져온 이메일
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
