package com.ll.quizzle.domain.admin.dto.request;

public record AdminLoginRequest(
	String adminEmail,
	String adminPassword
) {
}
