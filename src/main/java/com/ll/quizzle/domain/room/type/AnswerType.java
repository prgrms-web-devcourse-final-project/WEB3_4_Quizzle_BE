package com.ll.quizzle.domain.room.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnswerType {
    MULTIPLE_CHOICE("객관식"),
    TRUE_FALSE("O/X");

    private final String description;
}
