package com.ll.quizzle.domain.point.type;

public enum PointReason {
	NICKNAME_CHANGE("닉네임 변경"),
	LEVEL_UP("레벨업 보상"),
	// 추가 가능
	;

	private final String description;

	PointReason(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
