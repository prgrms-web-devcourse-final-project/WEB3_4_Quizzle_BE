package com.ll.quizzle.domain.member.dto;

public record OAuth2Response(
	String email,
	String name,
	String status,
	String role,
	String provider,
	String oauthId,
	boolean isMember,
	boolean isAdmin,
	String accessToken,
	String refreshToken
) {
}
