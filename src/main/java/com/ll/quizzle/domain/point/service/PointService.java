package com.ll.quizzle.domain.point.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.domain.point.dto.PointHistoryRequestDto;
import com.ll.quizzle.domain.point.dto.PointHistoryResponseDto;
import com.ll.quizzle.domain.point.entity.Point;
import com.ll.quizzle.domain.point.repository.PointRepository;
import com.ll.quizzle.domain.point.type.PointReason;
import com.ll.quizzle.domain.point.type.PointType;
import com.ll.quizzle.standard.page.dto.PageDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {

	private final PointRepository pointRepository;
	private final MemberRepository memberRepository;

	public PageDto<PointHistoryResponseDto> getPointHistoriesWithPage(Long memberId,
		PointHistoryRequestDto requestDto) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

		//TODO: 로그인한 사용자와 memberId 일치 여부 검증 (Security 적용 후 추가)

		Pageable pageable = PageRequest.of(requestDto.page(), requestDto.size());

		Page<Point> page;

		if (requestDto.type() != PointType.ALL) {
			page = pointRepository.findPageByMemberAndTypeOrderByCreateDateDesc(member, requestDto.type(), pageable);
		} else {
			page = pointRepository.findPageByMemberOrderByCreateDateDesc(member, pageable);
		}

		return new PageDto<>(page.map(PointHistoryResponseDto::from));
	}

	//포인트 사용
	public void usePoint(Member member, int amount, PointReason reason) {
		member.decreasePoint(amount);
		Point point = Point.use(member, amount, reason);
		pointRepository.save(point);
	}

	//포인트 획득
	public void gainPoint(Member member, int amount, PointReason reason) {
		member.increasePoint(amount);
		Point point = Point.gain(member, amount, reason);
		pointRepository.save(point);
	}

}
