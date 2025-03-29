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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Point extends BaseTime {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	private int amount;

	@Enumerated(EnumType.STRING)
	private PointType type;

	@Enumerated(EnumType.STRING)
	private PointReason reason;

	public String getDescription() {
		return reason.getDescription();
	}

	public static Point use(Member member, int amount, PointReason reason) {
		return new Point(member, -amount, PointType.USE, reason);
	}

	public static Point gain(Member member, int amount, PointReason reason) {
		return new Point(member, amount, PointType.REWARD, reason);
	}
}
