package com.ll.quizzle.domain.avatar.dto.response;

import com.ll.quizzle.domain.avatar.entity.Avatar;
import com.ll.quizzle.domain.avatar.type.AvatarStatus;

public record AvatarPurchaseResponse(
    Long id,
    String fileName,
    int price,
    AvatarStatus status,
    String url
) {
    public static AvatarPurchaseResponse from(Avatar avatar) {
        return new AvatarPurchaseResponse(
            avatar.getId(),
            avatar.getFileName(),
            avatar.getPrice(),
            avatar.getStatus(),
            avatar.getUrl()
        );
    }
}
