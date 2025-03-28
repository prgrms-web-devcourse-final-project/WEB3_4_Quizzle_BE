package com.ll.quizzle.domain.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ll.quizzle.domain.room.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
