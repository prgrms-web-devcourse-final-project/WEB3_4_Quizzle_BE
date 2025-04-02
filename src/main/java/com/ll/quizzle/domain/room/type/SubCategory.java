package com.ll.quizzle.domain.room.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubCategory {
    PHYSICS("물리학"),
    CHEMISTRY("화학"),
    BIOLOGY("생물학"),
    
    WORLD_HISTORY("세계사"),
    KOREAN_HISTORY("한국사"),
    
    KOREAN("한국어"),
    ENGLISH("영어"),
    
    CURRENT_AFFAIRS("시사"),
    CULTURE("문화"),
    SPORTS("스포츠");
    
    private final String description;
}
