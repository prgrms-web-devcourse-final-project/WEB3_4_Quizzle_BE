package com.ll.quizzle.global.initData;

import com.ll.quizzle.domain.avatar.entity.Avatar;
import com.ll.quizzle.domain.avatar.repository.AvatarRepository;
import com.ll.quizzle.domain.avatar.type.AvatarStatus;
import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.domain.member.type.Role;
import com.ll.quizzle.domain.room.dto.request.RoomCreateRequest;
import com.ll.quizzle.domain.room.service.RoomService;
import com.ll.quizzle.domain.room.type.AnswerType;
import com.ll.quizzle.domain.room.type.Difficulty;
import com.ll.quizzle.domain.room.type.MainCategory;
import com.ll.quizzle.domain.room.type.SubCategory;
import com.ll.quizzle.domain.system.service.SystemService;
import com.ll.quizzle.global.security.oauth2.entity.OAuth;
import com.ll.quizzle.global.security.oauth2.repository.OAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.ll.quizzle.global.exceptions.ErrorCode.AVATAR_NOT_FOUND;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class BaseInitData {
    private final MemberRepository memberRepository;
    private final OAuthRepository oAuthRepository;
    private final AvatarRepository avatarRepository;
    private final SystemService systemService;
    private final RoomService roomService;

    @Autowired
    @Lazy
    private BaseInitData self;

    @Bean
    public ApplicationRunner baseInitDataRunner() {
        return args -> {
            self.avatarInit();
            self.init();
            self.adminInit();
        };
    }

    @Transactional
    public void init() {
        if (memberRepository.count() > 0) {
            return;
        }

        Avatar defaultAvatar = avatarRepository.findByFileName("새콩이")
                .orElseThrow(AVATAR_NOT_FOUND::throwServiceException);

        List<Member> members = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            String nickname = "test" + i;
            String email = "test" + i + "@email.com";
            String provider = (i % 2 == 0) ? "google" : "kakao";

            Member member = Member.create(nickname, email, defaultAvatar);
            members.add(member);

            OAuth oauth = OAuth.create(member, provider, String.valueOf(i));

            memberRepository.save(member);
            oAuthRepository.save(oauth);
        }

        for (Member owner : members) {
            roomService.createRoom(
                    owner.getId(),
                    new RoomCreateRequest(
                            "제목",
                            2,
                            Difficulty.EASY,
                            MainCategory.HISTORY,
                            SubCategory.WORLD_HISTORY,
                            AnswerType.MULTIPLE_CHOICE,
                            11,
                            null,
                            false
                    )
            );
        }
    }


    @Transactional
    public void adminInit() {
        if (memberRepository.findByEmail("admin@quizzle.com").isPresent()) {
            return;
        }

        if (memberRepository.findByEmail("member@quizzle.com").isPresent()) {
            return;
        }

        Avatar defaultAvatar = avatarRepository.findByFileName("새콩이")
                .orElseThrow(AVATAR_NOT_FOUND::throwServiceException);


        Member testAdmin = Member.create("admin", "admin@quizzle.com", defaultAvatar);
        testAdmin.changeRole(Role.ADMIN);

        Member testMember = Member.create("member", "member@quizzle.com", defaultAvatar);

        OAuth testAdminOauth = OAuth.create(testAdmin, "kakao", "51");
        OAuth testMemberOauth2 = OAuth.create(testMember, "google", "52");

        memberRepository.save(testAdmin);
        memberRepository.save(testMember);
        oAuthRepository.save(testAdminOauth);
        oAuthRepository.save(testMemberOauth2);
    }


    @Transactional
    public void avatarInit() {
        if (avatarRepository.count() > 0) return;

        Avatar defaultAvatar = Avatar.builder()
                .fileName("새콩이")
                .url("https://quizzle-avatars.s3.ap-northeast-2.amazonaws.com/%EA%B8%B0%EB%B3%B8+%EC%95%84%EB%B0%94%ED%83%80.png")
                .price(0)
                .status(AvatarStatus.OWNED)
                .build();

        Avatar nerdySaekong = Avatar.builder()
                .fileName("안경쓴 새콩이")
                .url("https://quizzle-avatars.s3.ap-northeast-2.amazonaws.com/%EC%95%88%EA%B2%BD%EC%93%B4+%EC%83%88%EC%BD%A9%EC%9D%B4.png")
                .price(300)
                .status(AvatarStatus.AVAILABLE)
                .build();

        avatarRepository.save(defaultAvatar);
        avatarRepository.save(nerdySaekong);
    }

}
