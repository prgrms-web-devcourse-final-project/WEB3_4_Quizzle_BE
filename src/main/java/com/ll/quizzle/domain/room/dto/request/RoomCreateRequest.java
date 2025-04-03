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
        if (title != null && title.length() > 30) {
            ErrorCode.ROOM_TITLE_TOO_LONG.throwServiceException();
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
        if (isPrivate && password == null) {
            ErrorCode.ROOM_PRIVATE_PASSWORD_REQUIRED.throwServiceException();
        }
        
        if (password != null) {
            if (password.length() != 4 || !password.matches("\\d{4}")) {
                ErrorCode.ROOM_PASSWORD_INVALID.throwServiceException();
            }
        }
    }
} 