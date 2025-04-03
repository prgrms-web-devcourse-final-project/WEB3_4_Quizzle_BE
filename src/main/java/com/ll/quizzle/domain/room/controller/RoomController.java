package com.ll.quizzle.domain.room.controller;

import com.ll.quizzle.domain.room.dto.request.RoomCreateRequest;
import com.ll.quizzle.domain.room.dto.response.RoomResponse;
import com.ll.quizzle.domain.room.service.RoomService;
import com.ll.quizzle.global.response.RsData;
import com.ll.quizzle.global.request.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rooms")
@Tag(name = "방 관리", description = "방 생성, 조회, 입장, 퇴장 등의 API")
public class RoomController {
    
    private final RoomService roomService;
    private final Rq rq;
    
    @PostMapping
    @Operation(summary = "방 생성", description = "새로운 게임 방을 생성합니다.")
    public RsData<RoomResponse> createRoom(
            @RequestBody RoomCreateRequest request
    ) {
        RoomResponse response = roomService.createRoom(rq.getActor().getId(), request);
        return RsData.success(HttpStatus.CREATED, response);
    }
    
    @GetMapping
    @Operation(summary = "활성화된 방 목록 조회", description = "현재 활성화된 모든 방의 목록을 조회합니다.")
    public RsData<List<RoomResponse>> activeRooms() {
        List<RoomResponse> responses = roomService.getActiveRooms();
        return RsData.success(HttpStatus.OK, responses);
    }
    
    @PostMapping("/{roomId}/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "방 입장", description = "특정 방에 입장합니다. 비공개 방인 경우 비밀번호가 필요합니다.")
    public void joinRoom(
            @PathVariable Long roomId,
            @RequestParam(required = false) String password
    ) {
        roomService.joinRoom(roomId, rq.getActor().getId(), password);
    }
    
    @PostMapping("/{roomId}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "방 퇴장", description = "현재 입장해 있는 방에서 퇴장합니다. 방장이 퇴장하면 방이 삭제됩니다.")
    public void leaveRoom(
            @PathVariable Long roomId
    ) {
        roomService.leaveRoom(roomId, rq.getActor().getId());
    }
    
    @PostMapping("/{roomId}/ready")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "준비 상태 토글", description = "게임 준비 상태를 토글합니다. 방장은 준비 상태가 적용되지 않습니다.")
    public void toggleReady(
            @PathVariable Long roomId
    ) {
        roomService.toggleReady(roomId, rq.getActor().getId());
    }
    
    @PostMapping("/{roomId}/start")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "게임 시작", description = "게임을 시작합니다. 방장만 시작할 수 있으며, 모든 플레이어가 준비 상태여야 합니다.")
    public void startGame(
            @PathVariable Long roomId
    ) {
        roomService.startGame(roomId, rq.getActor().getId());
    }
}
