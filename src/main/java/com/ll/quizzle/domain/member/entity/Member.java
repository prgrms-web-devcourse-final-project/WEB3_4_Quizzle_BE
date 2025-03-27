package com.ll.quizzle.domain.member.entity;

import com.ll.quizzle.domain.member.type.Role;
import com.ll.quizzle.global.jpa.entity.BaseEntity;
import com.ll.quizzle.global.security.oauth2.entity.OAuth;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member extends BaseEntity {
    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private int level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private int exp;

    @Column(nullable = false)
    private String profilePath;

    @Column(nullable = false)
    private int pointBalance;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @Builder.Default
    private List<OAuth> oauths = new ArrayList<>();

    public String getUserRole() {
        return this.role.name();
    }

    public OAuth getFirstOAuth() {
        return this.oauths.isEmpty() ? null : this.oauths.get(0);
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }


    public boolean isMember() {
        return this.role == Role.MEMBER;
    }

    /**
     * 포인트 증가
     *
     * @param amount 증가할 포인트 양 (양수)
     */
    public void increasePoint(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("증가 포인트는 0보다 커야 합니다.");
        }
        this.pointBalance += amount;
    }

    /**
     * 포인트 차감
     *
     * @param amount 차감할 포인트 양 (양수)
     */
    public void decreasePoint(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("차감 포인트는 0보다 커야 합니다.");
        }

        if (this.pointBalance < amount) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }

        this.pointBalance -= amount;
    }
}