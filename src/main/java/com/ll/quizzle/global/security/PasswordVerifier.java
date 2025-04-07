package com.ll.quizzle.global.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ll.quizzle.global.config.SystemProperties;
import com.ll.quizzle.global.exceptions.ServiceException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class PasswordVerifier {
	private final PasswordEncoder passwordEncoder;
	private final SystemProperties systemProperties;

	// 시스템 이메일 검증
	public void verifySystemEmail(String inputEmail) {
		if (!systemProperties.getSystemEmail().equals(inputEmail)) {
			throw new ServiceException(HttpStatus.UNAUTHORIZED, "시스템 관리자 이메일이 일치하지 않습니다.");
		}
	}

	// 시스템 비밀번호 검증
	public void verifySystemPassword(String inputPassword) {
		if (!passwordEncoder.matches(inputPassword, systemProperties.getSystemPasswordHash())) {
			throw new ServiceException(HttpStatus.UNAUTHORIZED, "시스템 관리자 비밀번호가 일치하지 않습니다.");
		}
	}

	// 2차 비밀번호 검증
	public void verifySecondaryPassword(String inputPassword) {
		if (!passwordEncoder.matches(inputPassword, systemProperties.getSecondaryPasswordHash())) {
			throw new ServiceException(HttpStatus.UNAUTHORIZED, "2차 비밀번호가 일치하지 않습니다.");
		}
	}
}
