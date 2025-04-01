package com.ll.quizzle.domain.member.service;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.global.exceptions.ErrorCode;
import com.ll.quizzle.global.exceptions.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberExpService {

    private final MemberRepository memberRepository;

    public MemberExpService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
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
        int newExp = member.getExp() + score;
        member.updateExp(newExp);
        // 필요에 따라 레벨업 로직 추가 (예: 100 EXP마다 레벨업)
        memberRepository.save(member);
    }
}
