package com.ll.quizzle.domain.point.entity;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.point.type.PointReason;
import com.ll.quizzle.domain.point.type.PointType;
import com.ll.quizzle.global.jpa.entity.BaseTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point extends BaseTime {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	private int amount;

	@Enumerated(EnumType.STRING)
	private PointType type;

	@Enumerated(EnumType.STRING)
	private PointReason reason;

	@Builder
	public Point(Member member, int amount, PointType type, PointReason reason) {
		this.member = member;
		this.amount = amount;
		this.type = type;
		this.reason = reason;
	}

	public String getDescription() {
		return reason.getDescription();
	}
}
