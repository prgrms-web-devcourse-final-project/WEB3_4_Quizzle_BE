package com.ll.quizzle.domain.friend.dto.response;

import com.ll.quizzle.domain.friend.entity.FriendOffer;

import java.time.LocalDateTime;

public record FriendRequestListResponse(
        long memberId,
        String nickname,
        LocalDateTime requestedAt
) {
    public static FriendRequestListResponse from(FriendOffer friendOffer) {
        return new FriendRequestListResponse(
                friendOffer.getFromMember().getId(),
                friendOffer.getFromMember().getNickname(),
                friendOffer.getCreateDate()
        );
    }
}
