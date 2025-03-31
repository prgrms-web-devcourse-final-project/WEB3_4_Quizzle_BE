package com.ll.quizzle.domain.member.service;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
public class MemberExpService {

    private final MemberRepository memberRepository;

    public MemberExpService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * 퀴즈 결과에 따라 멤버의 EXP를 갱신하는 메서드.
     * Redis에서 전달받은 userId(문자열)를 Long으로 변환하여 조회합니다.
     *
     * @param userId 문자열 형태의 멤버 ID (예: "1", "2", ...)
     * @param score  획득한 점수
     */
    public void updateMemberExp(String userId, int score) {
        Long memberId = Long.parseLong(userId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("멤버를 찾을 수 없습니다."));
        int newExp = member.getExp() + score;
        member.updateExp(newExp);
        // 필요에 따라 레벨업 로직 추가 (예: 100 EXP마다 레벨업)
        memberRepository.save(member);
    }
}
