package com.ll.quizzle.global.initdata;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import com.ll.quizzle.domain.avatar.entity.Avatar;
import com.ll.quizzle.domain.avatar.repository.AvatarRepository;
import com.ll.quizzle.domain.avatar.type.AvatarStatus;

import lombok.RequiredArgsConstructor;

@Profile("prod")
@Configuration
@RequiredArgsConstructor
public class ProdInitData {

    private final AvatarRepository avatarRepository;

    @Bean
    public ApplicationRunner prodInitDataRunner() {
        return args -> initAvatars();
    }

    @Transactional
    public void initAvatars() {
        if (avatarRepository.existsByFileName("새콩이")) {
            Avatar saekong = Avatar.builder()
                .fileName("새콩이")
                .url("https://quizzle-avatars.s3.ap-northeast-2.amazonaws.com/%EA%B8%B0%EB%B3%B8+%EC%95%84%EB%B0%94%ED%83%80.png")
                .price(0)
                .status(AvatarStatus.OWNED)
                .build();

            avatarRepository.save(saekong);
        }

        if (avatarRepository.existsByFileName("안경쓴 새콩이")) {
            Avatar nerdySaekong = Avatar.builder()
                .fileName("안경쓴 새콩이")
                .url("https://quizzle-avatars.s3.ap-northeast-2.amazonaws.com/%EC%95%88%EA%B2%BD%EC%93%B4+%EC%83%88%EC%BD%A9%EC%9D%B4.png")
                .price(300)
                .status(AvatarStatus.AVAILABLE)
                .build();

            avatarRepository.save(nerdySaekong);
        }
    }
}
