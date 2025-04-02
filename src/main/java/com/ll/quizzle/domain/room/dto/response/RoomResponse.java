package com.ll.quizzle.domain.room.dto.response;

import com.ll.quizzle.domain.room.entity.Room;
import com.ll.quizzle.domain.room.type.QuizCategory;
import com.ll.quizzle.domain.room.type.RoomStatus;
import com.ll.quizzle.domain.quiz.enums.Difficulty;

import java.util.Set;

public record RoomResponse(
    Long id,
    String title,
    Long ownerId,
    String ownerNickname,
    int capacity,
    int currentPlayers,
    RoomStatus status,
    Difficulty difficulty,
    QuizCategory category,
    String password,
    boolean isPrivate,
    Set<Long> players,
    Set<Long> readyPlayers
) {
    public static RoomResponse from(Room room) {
        return new RoomResponse(
            room.getId(),
            room.getTitle(),
            room.getOwner().getId(),
            room.getOwner().getNickname(),
            room.getCapacity(),
            room.getPlayers().size(),
            room.getStatus(),
            room.getDifficulty(),
            room.getCategory(),
            room.getPassword(),
            room.isPrivate(),
            room.getPlayers(),
            room.getReadyPlayers()
        );
    }
} 