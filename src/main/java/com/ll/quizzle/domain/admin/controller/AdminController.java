package com.ll.quizzle.domain.admin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ll.quizzle.domain.admin.dto.request.AdminLoginRequest;
import com.ll.quizzle.domain.admin.service.AdminService;
import com.ll.quizzle.global.exceptions.ErrorCode;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
	private final AdminService adminService;

	@PostMapping("/login")
	@ResponseStatus(HttpStatus.OK)
	public void login(@RequestBody AdminLoginRequest request, HttpSession session) {
		boolean isAuthenticated = adminService.authenticate(request, session);

		if (!isAuthenticated) {
			throw ErrorCode.INVALID_LOGIN_CREDENTIALS.throwServiceException();
		}
	}

	@DeleteMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "관리자 로그아웃", description = "관리자 계정을 로그아웃합니다.")
	public void logout(HttpSession session) {
		adminService.logout(session);
	}
}
