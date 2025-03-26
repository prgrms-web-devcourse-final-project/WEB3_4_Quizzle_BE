package com.ll.quizzle.domain.point.dto;

import java.time.LocalDateTime;

import com.ll.quizzle.domain.point.entity.Point;

public record PointHistoryResponseDto(
	String type,
	int amount,
	String description,
	LocalDateTime createdAt
) {
	public static PointHistoryResponseDto from(Point point) {
		return new PointHistoryResponseDto(
			point.getType().name(),
			point.getAmount(),
			point.getReason().getDescription(),
			point.getCreateDate()
		);
	}
}
