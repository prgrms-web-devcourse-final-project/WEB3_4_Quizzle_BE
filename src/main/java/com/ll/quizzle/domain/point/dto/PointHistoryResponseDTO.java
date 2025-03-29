package com.ll.quizzle.domain.point.dto;

import java.time.LocalDateTime;

import com.ll.quizzle.domain.point.entity.Point;

public record PointHistoryResponseDTO(
	String type,
	int amount,
	String description,
	LocalDateTime createdAt
) {
	public static PointHistoryResponseDTO from(Point point) {
		return new PointHistoryResponseDTO(
			point.getType().name(),
			point.getAmount(),
			point.getReason().getDescription(),
			point.getCreateDate()
		);
	}
}
