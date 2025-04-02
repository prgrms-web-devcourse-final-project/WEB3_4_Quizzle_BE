package com.ll.quizzle.domain.room.repository;

import com.ll.quizzle.domain.room.entity.Room;
import com.ll.quizzle.domain.room.type.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByStatusNot(RoomStatus status);
}
