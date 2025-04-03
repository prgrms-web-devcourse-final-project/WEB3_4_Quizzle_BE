package com.ll.quizzle.domain.admin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ll.quizzle.domain.admin.dto.request.AdminLoginRequest;
import com.ll.quizzle.domain.admin.service.AdminService;
import com.ll.quizzle.global.exceptions.ErrorCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
	private final AdminService adminService;

	@PostMapping("/login")
	@ResponseStatus(HttpStatus.OK)
	public void login(@RequestBody AdminLoginRequest request) {
		boolean isAuthenticated = adminService.authenticate(request);

		if (!isAuthenticated) {
			throw ErrorCode.INVALID_LOGIN_CREDENTIALS.throwServiceException();
		}
	}
}
