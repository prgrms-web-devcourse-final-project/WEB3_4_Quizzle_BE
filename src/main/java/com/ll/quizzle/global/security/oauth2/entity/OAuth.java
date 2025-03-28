package com.ll.quizzle.global.security.oauth2.entity;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "oauth", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "oauth_id"})
})
public class OAuth extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", unique = true)
    private Member member;

    @Column(nullable = false)
    private String provider;

    @Column(name = "oauth_id", nullable = false)
    private String oauthId;

    public static OAuth create(Member member, String provider, String oauthId) {
        return OAuth.builder()
                .member(member)
                .provider(provider)
                .oauthId(oauthId)
                .build();
    }
}
