package com.ll.quizzle.domain.member.entity;

import com.ll.quizzle.domain.member.type.Role;
import com.ll.quizzle.global.jpa.entity.BaseTime;
import com.ll.quizzle.global.security.oauth2.entity.OAuth;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.ll.quizzle.global.exceptions.ErrorCode.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTime {
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

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private OAuth oauth;

    @Builder
    private Member(String nickname, String email, int level, Role role, int exp, String profilePath, int pointBalance) {
        this.nickname = nickname;
        this.email = email;
        this.level = level;
        this.role = role;
        this.exp = exp;
        this.profilePath = profilePath;
        this.pointBalance = pointBalance;
    }

    public String getUserRole() {
        return this.role.name();
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public boolean isMember() {
        return this.role == Role.MEMBER;
    }

    public void increasePoint(int amount) {
        if (amount <= 0) {
            POINT_INCREASE_AMOUNT_INVALID.throwServiceException();
        }
        this.pointBalance += amount;
    }

    public void decreasePoint(int amount) {
        if (amount <= 0) {
            POINT_DECREASE_AMOUNT_INVALID.throwServiceException();
        }
        if (this.pointBalance < amount) {
            POINT_NOT_ENOUGH.throwServiceException();
        }
        this.pointBalance -= amount;
    }

    public void updateExp(int newExp) {
        this.exp = newExp;
        int calculatedLevel = newExp / 100;
        if (calculatedLevel > this.level) {
            this.level = calculatedLevel;
        }
    }

    public static Member create(String nickname, String email) {
        return Member.builder()
            .nickname(nickname)
            .email(email)
            .level(0)
            .role(Role.MEMBER)
            .exp(0)
            .profilePath("기본경로")
            .pointBalance(0)
            .build();
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }
}
