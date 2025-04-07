package com.ll.quizzle.global.security.aop;

import java.util.stream.Stream;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.ll.quizzle.domain.system.dto.request.RoleChangeRequest;
import com.ll.quizzle.global.config.SystemProperties;
import com.ll.quizzle.global.exceptions.ServiceException;
import com.ll.quizzle.global.security.annotation.RequireSecondaryPassword;
import com.ll.quizzle.global.security.oauth2.dto.SecurityUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class SecondaryPasswordAspect {
	private final SystemProperties systemProperties;
	private final BCryptPasswordEncoder passwordEncoder;

	@Around("@annotation(requireSecondaryPassword)")
	public Object validateSecondaryPassword(ProceedingJoinPoint joinPoint,
		RequireSecondaryPassword requireSecondaryPassword) throws Throwable {

		String secondaryPassword = extractSecondaryPasswordFromRequest(joinPoint);
		if (secondaryPassword == null) {
			throw new ServiceException(HttpStatus.BAD_REQUEST, "2차 비밀번호를 입력해주세요.");
		}

		SecurityUser user = getCurrentUser();
		if (isSystemAdmin(user)) {
			validateSystemAdminPassword(secondaryPassword);
		}

		return joinPoint.proceed();
	}

	private SecurityUser getCurrentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof SecurityUser)) {
			throw new ServiceException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
		}
		return (SecurityUser) auth.getPrincipal();
	}

	private boolean isSystemAdmin(SecurityUser user) {
		return systemProperties.getSystemEmail().equals(user.getEmail());
	}

	private void validateSystemAdminPassword(String secondaryPassword) {
		if (!passwordEncoder.matches(secondaryPassword, systemProperties.getSecondaryPasswordHash())) {
			throw new ServiceException(HttpStatus.UNAUTHORIZED, "2차 비밀번호가 일치하지 않습니다.");
		}
	}

	private String extractSecondaryPasswordFromRequest(ProceedingJoinPoint joinPoint) {
		return Stream.of(joinPoint.getArgs())
			.filter(arg -> arg instanceof RoleChangeRequest)
			.map(arg -> ((RoleChangeRequest) arg).secondaryPassword())
			.findFirst()
			.orElse(null);
	}
}
