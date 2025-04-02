package com.ll.quizzle.domain.room.controller;

import com.ll.quizzle.domain.room.dto.request.RoomCreateRequest;
import com.ll.quizzle.domain.room.dto.response.RoomResponse;
import com.ll.quizzle.domain.room.service.RoomService;
import com.ll.quizzle.global.response.RsData;
import com.ll.quizzle.global.request.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rooms")
public class RoomController {
    
    private final RoomService roomService;
    private final Rq rq;
    
    @PostMapping
    public RsData<RoomResponse> createRoom(
            @Valid @RequestBody RoomCreateRequest request
    ) {
        RoomResponse response = roomService.createRoom(rq.getActor().getId(), request);
        return RsData.success(HttpStatus.CREATED, response);
    }
    
    @GetMapping
    public RsData<List<RoomResponse>> activeRooms() {
        List<RoomResponse> responses = roomService.getActiveRooms();
        return RsData.success(HttpStatus.OK, responses);
    }
    
    @PostMapping("/{roomId}/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void joinRoom(
            @PathVariable Long roomId,
            @RequestParam(required = false) String password
    ) {
        roomService.joinRoom(roomId, rq.getActor().getId(), password);
    }
    
    @PostMapping("/{roomId}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveRoom(
            @PathVariable Long roomId
    ) {
        roomService.leaveRoom(roomId, rq.getActor().getId());
    }
    
    @PostMapping("/{roomId}/ready")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void toggleReady(
            @PathVariable Long roomId
    ) {
        roomService.toggleReady(roomId, rq.getActor().getId());
    }
    
    @PostMapping("/{roomId}/start")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void startGame(
            @PathVariable Long roomId
    ) {
        roomService.startGame(roomId, rq.getActor().getId());
    }
}
