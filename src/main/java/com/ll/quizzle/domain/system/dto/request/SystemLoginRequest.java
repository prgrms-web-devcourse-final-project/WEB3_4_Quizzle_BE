package com.ll.quizzle.domain.system.dto.request;

public record SystemLoginRequest(
	String systemEmail,
	String systemPassword,
	String secondaryPassword
) {
	public static SystemLoginRequest of(
		String systemEmail,
		String systemPassword,
		String secondaryPassword
	) {
		return new SystemLoginRequest(systemEmail, systemPassword, secondaryPassword);
	}
}
