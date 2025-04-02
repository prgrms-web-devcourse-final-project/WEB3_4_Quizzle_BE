package com.ll.quizzle.domain.admin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ll.quizzle.domain.admin.dto.AdminLoginRequestDTO;
import com.ll.quizzle.domain.admin.service.AdminService;
import com.ll.quizzle.global.response.RsData;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
	private final AdminService adminService;

	@PostMapping("/login")
	public RsData<String> login(@RequestBody AdminLoginRequestDTO request) {
		boolean isAuthenticated = adminService.authenticate(request);

		if (!isAuthenticated) {
			throw new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.");
		}

		return RsData.success(HttpStatus.OK, "로그인에 성공했습니다.");
	}
}
