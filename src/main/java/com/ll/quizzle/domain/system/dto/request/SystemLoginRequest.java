package com.ll.quizzle.domain.system.dto.request;

public record SystemLoginRequest(
	String systemEmail,
	String systemPassword
) {
}
