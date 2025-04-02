package com.ll.quizzle.domain.room.dto.request;

import com.ll.quizzle.domain.room.type.MainCategory;
import com.ll.quizzle.domain.room.type.SubCategory;
import com.ll.quizzle.domain.room.type.Difficulty;
import com.ll.quizzle.global.exceptions.ErrorCode;

public record RoomCreateRequest(
    String title,
    int capacity,
    Difficulty difficulty,
    MainCategory mainCategory,
    SubCategory subCategory,
    String password,
    boolean isPrivate
) {
    public RoomCreateRequest {
        if (title == null || title.trim().isEmpty()) {
            ErrorCode.ROOM_TITLE_REQUIRED.throwServiceException();
        }
        if (capacity < 2 || capacity > 8) {
            ErrorCode.ROOM_CAPACITY_INVALID.throwServiceException();
        }
        if (difficulty == null) {
            ErrorCode.ROOM_DIFFICULTY_REQUIRED.throwServiceException();
        }
        if (mainCategory == null) {
            ErrorCode.ROOM_MAIN_CATEGORY_REQUIRED.throwServiceException();
        }
        if (subCategory == null) {
            ErrorCode.ROOM_SUB_CATEGORY_REQUIRED.throwServiceException();
        }
        if (isPrivate && (password == null || password.trim().isEmpty())) {
            ErrorCode.ROOM_PRIVATE_PASSWORD_REQUIRED.throwServiceException();
        }
    }
} 