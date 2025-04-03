package com.ll.quizzle.domain.room.dto.request;

import com.ll.quizzle.domain.room.type.MainCategory;
import com.ll.quizzle.domain.room.type.SubCategory;
import com.ll.quizzle.domain.room.type.Difficulty;
import com.ll.quizzle.global.exceptions.ErrorCode;

public record RoomUpdateRequest(
    String title,
    int capacity,
    Difficulty difficulty,
    MainCategory mainCategory,
    SubCategory subCategory,
    Integer password,
    boolean isPrivate
) {
    public RoomUpdateRequest {
        if (title != null) {
            if (title.trim().isEmpty()) {
                ErrorCode.ROOM_TITLE_EMPTY.throwServiceException();
            }
            if (title.length() > 30) {
                ErrorCode.ROOM_TITLE_TOO_LONG.throwServiceException();
            }
        }
        
        if (capacity != 0 && (capacity < 2 || capacity > 8)) {
            ErrorCode.ROOM_CAPACITY_INVALID.throwServiceException();
        }
        
        if (isPrivate && password == null) {
            ErrorCode.ROOM_PRIVATE_PASSWORD_REQUIRED.throwServiceException();
        }
        
        if (password != null) {
            String passwordStr = String.format("%04d", password);
            if (passwordStr.length() != 4 || password < 0 || password > 9999) {
                ErrorCode.ROOM_PASSWORD_INVALID.throwServiceException();
            }
        }
    }
} 