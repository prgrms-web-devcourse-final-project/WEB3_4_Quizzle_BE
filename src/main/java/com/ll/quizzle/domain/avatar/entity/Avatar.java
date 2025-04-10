package com.ll.quizzle.domain.avatar.entity;

import com.ll.quizzle.domain.avatar.type.AvatarStatus;
import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Avatar extends BaseEntity {

    private String fileName;
    private String url;
    private int price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private AvatarStatus status;

    @Builder
    public Avatar(String fileName, String url, int price, Member member, AvatarStatus status) {
        this.fileName = fileName;
        this.url = url;
        this.price = price;
        this.member = member;
        this.status = status;
    }

    public void purchase(Member member) {
        this.member = member;
        this.status = AvatarStatus.OWNED;
    }

    public boolean isOwned() {
        return this.status == AvatarStatus.OWNED;
    }
}
