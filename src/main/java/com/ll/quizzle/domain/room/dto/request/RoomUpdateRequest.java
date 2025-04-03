package com.ll.quizzle.domain.room.dto.request;

import com.ll.quizzle.domain.room.type.Difficulty;
import com.ll.quizzle.domain.room.type.MainCategory;
import com.ll.quizzle.domain.room.type.SubCategory;
import com.ll.quizzle.global.exceptions.ErrorCode;

public record RoomUpdateRequest(
    String title,
    int capacity,
    Difficulty difficulty,
    MainCategory mainCategory,
    SubCategory subCategory,
    String password,
    boolean isPrivate
) {
    public RoomUpdateRequest {
        if (title != null && title.trim().isEmpty()) {
            ErrorCode.ROOM_TITLE_EMPTY.throwServiceException();
        }
        if (capacity != 0 && (capacity < 2 || capacity > 8)) {
            ErrorCode.ROOM_CAPACITY_INVALID.throwServiceException();
        }
        if (isPrivate && (password == null || password.trim().isEmpty())) {
            ErrorCode.ROOM_PRIVATE_PASSWORD_REQUIRED.throwServiceException();
        }
    }
} 