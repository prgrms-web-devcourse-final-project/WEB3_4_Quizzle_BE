package com.ll.quizzle.domain.room.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.domain.room.dto.request.RoomCreateRequest;
import com.ll.quizzle.domain.room.dto.response.RoomResponse;
import com.ll.quizzle.domain.room.entity.Room;
import com.ll.quizzle.domain.room.repository.RoomRepository;
import com.ll.quizzle.domain.room.type.AnswerType;
import com.ll.quizzle.domain.room.type.Difficulty;
import com.ll.quizzle.domain.room.type.MainCategory;
import com.ll.quizzle.domain.room.type.RoomStatus;
import com.ll.quizzle.domain.room.type.SubCategory;
import com.ll.quizzle.global.exceptions.ServiceException;
import com.ll.quizzle.global.socket.core.MessageService;
import com.ll.quizzle.global.socket.core.MessageServiceFactory;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;
    
    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private MessageServiceFactory messageServiceFactory;
    
    @Mock
    private MessageService messageService;
    
    @Mock
    private RoomBlacklistService blacklistService;
    
    @InjectMocks
    private RoomService roomService;
    
    private Member testOwner;
    private Room testRoom;
    
    @BeforeEach
    void setUp() {
        testOwner = Member.create("테스트유저", "test@example.com");
        
        ReflectionTestUtils.setField(testOwner, "id", 1L);
        
        Room roomTemp = Room.builder()
                .title("테스트 방")
                .owner(testOwner)
                .capacity(4)
                .difficulty(Difficulty.NORMAL)
                .mainCategory(MainCategory.GENERAL_KNOWLEDGE)
                .subCategory(SubCategory.CULTURE)
                .answerType(AnswerType.MULTIPLE_CHOICE)
                .problemCount(10)
                .password("1234")
                .build();
        
        ReflectionTestUtils.setField(roomTemp, "id", 1L);
        ReflectionTestUtils.setField(roomTemp, "status", RoomStatus.WAITING);
        
        testRoom = spy(roomTemp);
    }
    
    @Test
    @DisplayName("방 생성 테스트")
    void createRoomTest() {
        // given
        RoomCreateRequest request = new RoomCreateRequest(
                "테스트 방",
                4,
                Difficulty.NORMAL,
                MainCategory.GENERAL_KNOWLEDGE,
                SubCategory.CULTURE,
                null,
                false
        );
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testOwner));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        
        // when
        RoomResponse response = roomService.createRoom(1L, request);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("테스트 방");
        assertThat(response.difficulty()).isEqualTo(Difficulty.NORMAL);
        assertThat(response.mainCategory()).isEqualTo(MainCategory.GENERAL_KNOWLEDGE);
        assertThat(response.subCategory()).isEqualTo(SubCategory.CULTURE);
        
        verify(memberRepository).findById(1L);
        verify(roomRepository).save(any(Room.class));
    }
    
    @Test
    @DisplayName("비밀번호가 있는 방 생성 테스트")
    void createPrivateRoomTest() {
        // given
        RoomCreateRequest request = new RoomCreateRequest(
                "비밀방",
                4,
                Difficulty.NORMAL,
                MainCategory.GENERAL_KNOWLEDGE,
                SubCategory.CULTURE,
                "1234",
                true
        );
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testOwner));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        
        // when
        RoomResponse response = roomService.createRoom(1L, request);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("테스트 방");
        assertThat(response.isPrivate()).isTrue();
        
        verify(memberRepository).findById(1L);
        verify(roomRepository).save(any(Room.class));
    }
    
    @Test
    @DisplayName("활성화된 방 목록 조회 테스트")
    void getActiveRoomsTest() {
        // given
        List<Room> rooms = Arrays.asList(testRoom);
        when(roomRepository.findByStatusNot(RoomStatus.FINISHED)).thenReturn(rooms);
        
        // when
        List<RoomResponse> responses = roomService.getActiveRooms();
        
        // then
        assertThat(responses).isNotNull().hasSize(1);
        assertThat(responses.get(0).title()).isEqualTo("테스트 방");
        
        verify(roomRepository).findByStatusNot(RoomStatus.FINISHED);
    }
    
    @Test
    @DisplayName("방 입장 테스트 - 성공 (비밀번호 있다고 가정)")
    void joinRoomSuccessTest() {
        // given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(blacklistService.isBlacklisted(1L, 2L)).thenReturn(false);
        when(messageServiceFactory.getRoomService()).thenReturn(messageService);
        
        Member normalMember = Member.create("일반유저", "user@example.com");
        
        ReflectionTestUtils.setField(normalMember, "id", 2L);
        
        when(memberRepository.findById(2L)).thenReturn(Optional.of(normalMember));
        
        when(testRoom.validatePassword(any())).thenReturn(true);
        when(testRoom.isFull()).thenReturn(false);
        when(testRoom.hasPlayer(2L)).thenReturn(false);
        
        // when
        roomService.joinRoom(1L, 2L, null);
        
        // then
        verify(roomRepository).findById(1L);
        verify(blacklistService).isBlacklisted(1L, 2L);
        verify(testRoom).addPlayer(2L);
        verify(messageService).send(anyString(), any());
    }

    @Test
    @DisplayName("방 입장 테스트 - 비밀번호 오류")
    void joinRoomWrongPasswordTest() {
        // given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(blacklistService.isBlacklisted(1L, 2L)).thenReturn(false);
        when(testRoom.validatePassword("1234")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> roomService.joinRoom(1L, 2L, "1234"))
                .isInstanceOf(ServiceException.class);

        verify(testRoom, never()).addPlayer(anyLong());
    }
    
    @Test
    @DisplayName("방 입장 테스트 - 블랙리스트")
    void joinRoomBlacklistTest() {
        // given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(blacklistService.isBlacklisted(1L, 2L)).thenReturn(true);
        
        // when & then
        assertThatThrownBy(() -> roomService.joinRoom(1L, 2L, null))
            .isInstanceOf(ServiceException.class);
        
        verify(roomRepository).findById(1L);
        verify(blacklistService).isBlacklisted(1L, 2L);
        verify(testRoom, never()).addPlayer(anyLong());
    }
    
    @Test
    @DisplayName("방 퇴장 테스트 - 일반 유저")
    void leaveRoomNormalUserTest() {
        // given
        Member normalMember = Member.create("일반유저", "user@example.com");
        
        ReflectionTestUtils.setField(normalMember, "id", 2L);
        
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(normalMember));
        when(testRoom.isOwner(2L)).thenReturn(false);
        when(messageServiceFactory.getRoomService()).thenReturn(messageService);
        
        // when
        roomService.leaveRoom(1L, 2L);
        
        // then
        verify(roomRepository).findById(1L);
        verify(memberRepository).findById(2L);
        verify(testRoom).removePlayer(2L);
        verify(testRoom).isOwner(2L);
        verify(roomRepository, never()).delete(any(Room.class));
        verify(messageService).send(anyString(), any());
    }
    
    @Test
    @DisplayName("방 퇴장 테스트 - 방장 (다른 플레이어 X)")
    void leaveRoomOwnerWithoutPlayersTest() {
        // given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testOwner));
        when(testRoom.isOwner(1L)).thenReturn(true);
        when(messageServiceFactory.getRoomService()).thenReturn(messageService);
        
        Set<Long> emptyPlayerSet = new HashSet<>();
        when(testRoom.getPlayers()).thenReturn(emptyPlayerSet);
        
        // when
        roomService.leaveRoom(1L, 1L);
        
        // then
        verify(roomRepository).findById(1L);
        verify(memberRepository).findById(1L);
        verify(testRoom).removePlayer(1L);
        verify(testRoom).isOwner(1L);
        verify(testRoom).getPlayers();
        verify(roomRepository).delete(testRoom);
        verify(messageService).send(anyString(), any());
    }
    
    @Test
    @DisplayName("방 퇴장 테스트 - 방장 (다른 플레이어 O)")
    void leaveRoomOwnerWithPlayersTest() {
        // given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testOwner));
        when(testRoom.isOwner(1L)).thenReturn(true);
        when(messageServiceFactory.getRoomService()).thenReturn(messageService);
        
        Set<Long> playerSet = new HashSet<>();
        playerSet.add(2L);
        when(testRoom.getPlayers()).thenReturn(playerSet);
        
        Member newOwner = Member.create("새방장", "newowner@example.com");
        ReflectionTestUtils.setField(newOwner, "id", 2L);
        when(memberRepository.findById(2L)).thenReturn(Optional.of(newOwner));
        
        // when
        roomService.leaveRoom(1L, 1L);
        
        // then
        verify(roomRepository).findById(1L);
        verify(memberRepository).findById(1L);
        verify(testRoom).removePlayer(1L);
        verify(testRoom).isOwner(1L);
        verify(testRoom, atLeastOnce()).getPlayers();
        verify(memberRepository).findById(2L);
        verify(testRoom).changeOwner(newOwner);
        verify(roomRepository, never()).delete(testRoom);
        verify(messageService).send(anyString(), any());
    }
    
    @Test
    @DisplayName("준비 상태 토글 테스트")
    void toggleReadyTest() {
        // given
        Member normalMember = Member.create("일반유저", "user@example.com");
        
        ReflectionTestUtils.setField(normalMember, "id", 2L);
        
        Set<Long> readyPlayers = new HashSet<>();
        
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(normalMember));
        when(testRoom.hasPlayer(2L)).thenReturn(true);
        when(testRoom.getReadyPlayers()).thenReturn(readyPlayers);
        when(messageServiceFactory.getRoomService()).thenReturn(messageService);
        
        // when
        roomService.toggleReady(1L, 2L);
        
        // then
        verify(roomRepository).findById(1L);
        verify(memberRepository).findById(2L);
        verify(testRoom).playerReady(2L);
        verify(messageService).send(anyString(), any());
    }
    
    @Test
    @DisplayName("게임 시작 테스트 - 성공")
    void startGameSuccessTest() {
        // given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(testRoom.isOwner(1L)).thenReturn(true);
        when(testRoom.isAllPlayersReady()).thenReturn(true);
        when(messageServiceFactory.getRoomService()).thenReturn(messageService);
        
        // when
        roomService.startGame(1L, 1L);
        
        // then
        verify(roomRepository).findById(1L);
        verify(testRoom).isOwner(1L);
        verify(testRoom).startGame();
        verify(messageService).send(anyString(), any());
    }
    
    @Test
    @DisplayName("게임 시작 테스트 - 방장 아님")
    void startGameNotOwnerTest() {
        // given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(testRoom.isOwner(2L)).thenReturn(false);
        
        // when & then
        assertThatThrownBy(() -> roomService.startGame(1L, 2L))
            .isInstanceOf(ServiceException.class);
        
        verify(roomRepository).findById(1L);
        verify(testRoom).isOwner(2L);
        verify(testRoom, never()).startGame();
    }
    
    @Test
    @DisplayName("게임 시작 테스트 - 모두 준비 안됨")
    void startGameNotAllReadyTest() {
        // given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(testRoom.isOwner(1L)).thenReturn(true);
        when(testRoom.isAllPlayersReady()).thenReturn(false);
        
        // when & then
        assertThatThrownBy(() -> roomService.startGame(1L, 1L))
            .isInstanceOf(ServiceException.class);
        
        verify(roomRepository).findById(1L);
        verify(testRoom).isOwner(1L);
        verify(testRoom, never()).startGame();
    }
    
    @Test
    @DisplayName("게임 종료 테스트")
    void endGameTest() {
        // given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(messageServiceFactory.getRoomService()).thenReturn(messageService);
        
        // when
        roomService.endGame(1L);
        
        // then
        verify(roomRepository).findById(1L);
        verify(testRoom).endGame();
        verify(messageService).send(anyString(), any());
    }


} 