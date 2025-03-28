package com.ll.quizzle.domain.point.service;

import static com.ll.quizzle.global.exceptions.ErrorCode.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.domain.point.dto.PointHistoryRequestDTO;
import com.ll.quizzle.domain.point.dto.PointHistoryResponseDTO;
import com.ll.quizzle.domain.point.entity.Point;
import com.ll.quizzle.domain.point.repository.PointRepository;
import com.ll.quizzle.domain.point.type.PointReason;
import com.ll.quizzle.domain.point.type.PointType;
import com.ll.quizzle.global.exceptions.ErrorCode;
import com.ll.quizzle.global.request.Rq;
import com.ll.quizzle.standard.page.dto.PageDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {

	private final PointRepository pointRepository;
	private final MemberRepository memberRepository;
	private final Rq rq;

	public PageDto<PointHistoryResponseDTO> getPointHistoriesWithPage(Long memberId,
		PointHistoryRequestDTO requestDto) {

		Member member = memberRepository.findById(memberId)
			.orElseThrow(MEMBER_NOT_FOUND::throwServiceException);

		rq.assertIsOwner(memberId);

		Pageable pageable = PageRequest.of(requestDto.page(), requestDto.size());

		Page<Point> page;

		if (requestDto.type() != PointType.ALL) {
			page = pointRepository.findPageByMemberAndTypeOrderByCreateDateDesc(member, requestDto.type(), pageable);
		} else {
			page = pointRepository.findPageByMemberOrderByCreateDateDesc(member, pageable);
		}

		return new PageDto<>(page.map(PointHistoryResponseDTO::from));
	}

	// 포인트 사용
	public void usePoint(Member member, int amount, PointReason reason) {
		member.decreasePoint(amount); // ↓ 여기서 POINT_NOT_ENOUGH 예외 처리되어야 함
		Point point = Point.use(member, amount, reason);
		pointRepository.save(point);
	}

	// 포인트 획득
	public void gainPoint(Member member, int amount, PointReason reason) {
		member.increasePoint(amount);
		Point point = Point.gain(member, amount, reason);
		pointRepository.save(point);
	}

	// 정책 기반 포인트 처리
	public void applyPointPolicy(Member member, PointReason reason) {
		int amount = reason.getDefaultAmount();

		if (amount == 0) {
			throw ErrorCode.POINT_POLICY_NOT_FOUND.throwServiceException();
		}

		if (amount > 0) {
			gainPoint(member, amount, reason);
		} else {
			usePoint(member, -amount, reason);
		}
	}
}
