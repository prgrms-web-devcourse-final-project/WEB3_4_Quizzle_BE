package com.ll.quizzle.domain.member.service;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.domain.point.service.PointService;
import com.ll.quizzle.domain.point.type.PointReason;
import com.ll.quizzle.global.exceptions.ErrorCode;
import com.ll.quizzle.global.exceptions.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberExpService {

    private final MemberRepository memberRepository;
    private final PointService pointService; // PointService 주입

    public MemberExpService(MemberRepository memberRepository, PointService pointService) {
        this.memberRepository = memberRepository;
        this.pointService = pointService;
    }

    /**
     * 퀴즈 결과에 따라 멤버의 EXP를 갱신하는 메서드.
     *
     * @param userId 문자열 형태의 멤버 ID 또는 닉네임
     * @param score  획득한 점수
     */
    @Transactional
    public void updateMemberExp(String userId, int score) {
        Member member = memberRepository.findByNickname(userId)
                .orElseThrow(() -> new ServiceException(
                        ErrorCode.MEMBER_NOT_FOUND.getHttpStatus(),
                        ErrorCode.MEMBER_NOT_FOUND.getMessage()
                ));

        // 기존 레벨을 저장
        int oldLevel = member.getLevel();

        // EXP 갱신 및 레벨업 처리 (100 EXP마다 레벨업)
        int newExp = member.getExp() + score;
        member.updateExp(newExp);

        // 레벨이 상승했으면 LEVEL_UP 보상(500포인트) 지급
        if (member.getLevel() > oldLevel) {
            pointService.applyPointPolicy(member, PointReason.LEVEL_UP);
        }

        memberRepository.save(member);
    }
}
