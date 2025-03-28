package com.ll.quizzle.factory;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.point.entity.Point;
import com.ll.quizzle.domain.point.repository.PointRepository;
import com.ll.quizzle.domain.point.type.PointReason;

public class PointTestFactory {

	public static void createRewardPoint(Member member, PointRepository repo, int amount) {
		member.increasePoint(amount);
		repo.save(Point.gain(member, amount, PointReason.LEVEL_UP));
	}

	public static void createUsePoint(Member member, PointRepository repo, int amount) {
		member.decreasePoint(amount);
		repo.save(Point.use(member, amount, PointReason.NICKNAME_CHANGE));
	}

}
