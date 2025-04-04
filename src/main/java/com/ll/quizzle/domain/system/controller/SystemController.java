package com.ll.quizzle.domain.system.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ll.quizzle.domain.system.dto.request.RoleChangeRequest;
import com.ll.quizzle.domain.system.dto.request.SystemLoginRequest;
import com.ll.quizzle.domain.system.dto.response.SystemLoginResponse;
import com.ll.quizzle.domain.system.service.SystemService;
import com.ll.quizzle.global.response.RsData;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class SystemController {
	private final SystemService systemService;

	@PostMapping("/login")
	public RsData<SystemLoginResponse> login(
		@RequestBody SystemLoginRequest loginRequest,
		HttpServletResponse response
	) {
		return systemService.authenticate(loginRequest, response);
	}

	@PutMapping("/role")
	@PreAuthorize("hasRole('SYSTEM')")
	public RsData<Void> ChangeRole(
		@RequestBody RoleChangeRequest request,
		@AuthenticationPrincipal UserDetails userDetails
	) {
		return systemService.changeRole(request);
	}
}
