package com.ll.quizzle.domain.friend.dto.response;

import com.ll.quizzle.domain.friend.entity.Friend;

import java.time.LocalDateTime;

public record FriendListResponse(
        long memberId,
        String nickname,
        int level,
        LocalDateTime acceptedAt
) {
    public static FriendListResponse from(Friend friend) {
        return new FriendListResponse(
                friend.getFriend().getId(),
                friend.getFriend().getNickname(),
                friend.getFriend().getLevel(),
                friend.getCreateDate()
        );
    }
}
