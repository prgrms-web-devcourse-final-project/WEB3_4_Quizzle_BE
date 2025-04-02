package com.ll.quizzle.domain.admin.dto;

public record AdminLoginRequestDTO(
	String adminEmail,
	String adminPasswordHash
) {
}
