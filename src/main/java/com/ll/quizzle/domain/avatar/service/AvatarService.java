package com.ll.quizzle.domain.avatar.service;

import static com.ll.quizzle.global.exceptions.ErrorCode.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ll.quizzle.domain.avatar.dto.request.AvatarCreateRequest;
import com.ll.quizzle.domain.avatar.dto.response.AvatarPurchaseResponse;
import com.ll.quizzle.domain.avatar.entity.Avatar;
import com.ll.quizzle.domain.avatar.repository.AvatarRepository;
import com.ll.quizzle.domain.avatar.type.AvatarStatus;
import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.point.service.PointService;
import com.ll.quizzle.domain.point.type.PointReason;
import com.ll.quizzle.global.request.Rq;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AvatarService {

    private final AvatarRepository avatarRepository;
    private final PointService pointService;
    private final Rq rq;

    // 아바타 등록 매서드
    public void createAvatar(AvatarCreateRequest request) {

        Member admin = rq.getActor();

        if (!admin.isAdmin()) {
            throw FORBIDDEN_ACCESS.throwServiceException();
        }

        Avatar avatar = Avatar.builder()
            .fileName(request.fileName())
            .url(request.url())
            .price(request.price())
            .status(AvatarStatus.AVAILABLE)
            .build();

        avatarRepository.save(avatar);
    }

    // 아바타 구매 메서드
    public void purchaseAvatar(Long memberId, Long avatarId) {
        Member member = rq.assertIsOwner(memberId);

        Avatar avatar = avatarRepository.findById(avatarId)
            .orElseThrow(AVATAR_NOT_FOUND::throwServiceException);

        if (avatar.isOwned()) {
            throw AVATAR_ALREADY_OWNED.throwServiceException();
        }

        // 포인트 차감 및 로그 기록
        pointService.usePoint(member, avatar.getPrice(), PointReason.AVATAR_PURCHASE);

        // 아바타 구매 처리
        avatar.purchase(member);
        avatarRepository.save(avatar);
    }


    // 구매하지 않은 아바타 목록 조회
    public List<AvatarPurchaseResponse> getAvailableAvatars(Long memberId) {
        rq.assertIsOwner(memberId);
        return avatarRepository.findByStatus(AvatarStatus.AVAILABLE).stream()
            .map(AvatarPurchaseResponse::from)
            .toList();
    }

    // 소유한 아바타 목록 조회
    public List<AvatarPurchaseResponse> getOwnedAvatars(Long memberId) {
        Member member = rq.assertIsOwner(memberId);

		List<Avatar> ownedAvatars = new ArrayList<>(avatarRepository.findByMemberAndStatus(member, AvatarStatus.OWNED));

        Avatar currentAvatar = member.getAvatar();
        if (currentAvatar != null && ownedAvatars.stream().noneMatch(a -> a.getId().equals(currentAvatar.getId()))) {
            ownedAvatars.add(currentAvatar);
        }

        return ownedAvatars.stream()
            .map(AvatarPurchaseResponse::from)
            .toList();
    }

}
