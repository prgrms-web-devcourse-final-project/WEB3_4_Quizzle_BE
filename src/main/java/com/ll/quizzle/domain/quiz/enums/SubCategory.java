package com.ll.quizzle.domain.quiz.enums;

public enum SubCategory {
    // 과학 관련 서브 카테고리
    PHYSICS(MainCategory.SCIENCE),
    CHEMISTRY(MainCategory.SCIENCE),
    BIOLOGY(MainCategory.SCIENCE),

    // 역사 관련 서브 카테고리
    ANCIENT(MainCategory.HISTORY),
    MEDIEVAL(MainCategory.HISTORY),
    MODERN(MainCategory.HISTORY),

    // 언어 관련 서브 카테고리
    ENGLISH(MainCategory.LANGUAGE),
    FRENCH(MainCategory.LANGUAGE);

    private final MainCategory mainCategory;

    SubCategory(MainCategory mainCategory) {
        this.mainCategory = mainCategory;
    }

    public MainCategory getMainCategory() {
        return mainCategory;
    }
}
