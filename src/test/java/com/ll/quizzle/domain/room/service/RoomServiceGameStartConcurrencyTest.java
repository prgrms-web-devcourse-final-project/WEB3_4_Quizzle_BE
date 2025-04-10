package com.ll.quizzle.domain.room.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.domain.room.entity.Room;
import com.ll.quizzle.domain.room.repository.RoomRepository;
import com.ll.quizzle.domain.room.type.AnswerType;
import com.ll.quizzle.domain.room.type.Difficulty;
import com.ll.quizzle.domain.room.type.MainCategory;
import com.ll.quizzle.domain.room.type.RoomStatus;
import com.ll.quizzle.domain.room.type.SubCategory;
import com.ll.quizzle.global.exceptions.ServiceException;
import com.ll.quizzle.global.redis.lock.DistributedLockService;
import com.ll.quizzle.global.socket.service.WebSocketRoomMessageService;

@ExtendWith(MockitoExtension.class)
class RoomServiceConcurrencyTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private RoomBlacklistService blacklistService;
    @Mock
    private DistributedLockService redisLockService;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private WebSocketRoomMessageService roomMessageService;

    @InjectMocks
    private RoomService roomService;

    private MockedStatic<TransactionSynchronizationManager> mockedTransactionManager;
    private Room testRoom;
    private Member testOwner;
    private List<Member> testMembers;

    @BeforeEach
    void setUp() {
        // 트랜잭션 관리자 모킹
        mockedTransactionManager = mockStatic(TransactionSynchronizationManager.class);
        mockedTransactionManager.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                .thenAnswer(invocation -> null);

        // 방 소유자 설정
        testOwner = Member.create("방장", "owner@example.com");
        ReflectionTestUtils.setField(testOwner, "id", 1L);

        // 테스트 방 설정
        testRoom = Room.builder()
                .title("테스트 방")
                .owner(testOwner)
                .capacity(2) // 정원 2명
                .difficulty(Difficulty.NORMAL)
                .mainCategory(MainCategory.GENERAL_KNOWLEDGE)
                .subCategory(SubCategory.CULTURE)
                .answerType(AnswerType.MULTIPLE_CHOICE)
                .build();

        ReflectionTestUtils.setField(testRoom, "id", 1L);
        ReflectionTestUtils.setField(testRoom, "status", RoomStatus.WAITING);

        // 테스트 멤버 3명 설정
        testMembers = new ArrayList<>();
        for (int i = 2; i <= 4; i++) {
            Member member = Member.create("유저" + i, "user" + i + "@example.com");
            ReflectionTestUtils.setField(member, "id", (long) i);
            testMembers.add(member);
        }

        // Mock 기본 설정
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(redisLockService.acquireLock(anyString(), anyLong(), anyLong())).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        if (mockedTransactionManager != null) {
            mockedTransactionManager.close();
        }
    }

    @Test
    @DisplayName("방 입장 동시성 테스트 - 정원 초과 시 일부만 입장 성공")
    void joinRoomConcurrencyTest() {
        // given
        Room spyRoom = spy(testRoom);
        when(roomRepository.findRoomById(1L)).thenReturn(Optional.of(spyRoom));

        for (Member member : testMembers) {
            when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
            when(blacklistService.isBlacklisted(1L, member.getId())).thenReturn(false);
        }

        when(spyRoom.validatePassword(any())).thenReturn(true);

        // 첫 두 명은 입장 가능, 세 번째는 방이 가득 찬 상태로 시뮬레이션
        when(spyRoom.isFull()).thenReturn(false, false, true);
        when(spyRoom.hasPlayer(anyLong())).thenReturn(false);

        // when & then
        // 첫 번째 사용자 입장 시도
        roomService.joinRoom(1L, 2L, null);

        // 두 번째 사용자 입장 시도
        roomService.joinRoom(1L, 3L, null);

        // 세 번째 사용자 입장 시도 - 예외 발생 예상
        assertThatThrownBy(() -> roomService.joinRoom(1L, 4L, null))
                .isInstanceOf(ServiceException.class);

        // 검증
        verify(spyRoom, times(2)).addPlayer(anyLong()); // 두 명만 입장 성공
        verify(spyRoom, times(3)).isFull(); // isFull 메서드 3번 호출
    }

    @Test
    @DisplayName("방장 퇴장 시 소유권 경쟁 상태 테스트")
    void ownerLeaveWithConcurrentPlayerLeaveTest() {
        // given
        Room spyRoom = spy(testRoom);

        // 초기 플레이어 설정 (방장 + 일반 유저 2명)
        Set<Long> players = new HashSet<>();
        players.add(1L); // 방장
        players.add(2L); // 일반 유저1
        players.add(3L); // 일반 유저2
        ReflectionTestUtils.setField(spyRoom, "players", new HashSet<>(players));

        when(roomRepository.findRoomById(1L)).thenReturn(Optional.of(spyRoom));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testOwner));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(testMembers.get(0)));
        when(memberRepository.findById(3L)).thenReturn(Optional.of(testMembers.get(1)));

        when(spyRoom.isOwner(1L)).thenReturn(true);
        when(spyRoom.getId()).thenReturn(1L);

        // 시뮬레이션: 방장이 나가는 동시에 다른 유저도 나갈 때
        // 방장이 나가면 첫 번째로 찾은 유저(ID=2)에게 소유권이 넘어가야 함
        doAnswer(invocation -> {
            // removePlayer가 호출될 때 플레이어 목록에서 제거
            Long memberId = invocation.getArgument(0);
            Set<Long> currentPlayers = new HashSet<>(spyRoom.getPlayers());
            currentPlayers.remove(memberId);

            // 특정 상황 시뮬레이션: 방장이 나가고 ID=2인 유저만 남음 (ID=3인 유저도 동시에 나감)
            if (memberId == 1L) {
                currentPlayers.remove(3L); // ID=3인 유저도 동시에 나감
                when(spyRoom.getPlayers()).thenReturn(currentPlayers);
            } else {
                when(spyRoom.getPlayers()).thenReturn(currentPlayers);
            }

            return null;
        }).when(spyRoom).removePlayer(anyLong());

        // when
        roomService.leaveRoom(1L, 1L); // 방장 퇴장

        // then
        // 방장이 나가고 ID=2인 유저가 새 방장이 됨
        verify(spyRoom).removePlayer(1L);
        verify(memberRepository).findById(2L); // 새 방장을 찾기 위해 ID=2 조회

        // ID=3인 유저는 동시에 나갔으므로 새 방장 후보가 될 수 없음
        verify(spyRoom, never()).changeOwner(eq(testMembers.get(1)));
    }

    @Test
    @DisplayName("WebSocket 연결 끊김 처리 테스트")
    void handleDisconnectConcurrentTest() {
        // given
        List<Room> rooms = new ArrayList<>();
        Room room1 = spy(testRoom);
        Room room2 = spy(Room.builder().title("테스트 방2").owner(testOwner).capacity(4).build());
        ReflectionTestUtils.setField(room2, "id", 2L);
        rooms.add(room1);
        rooms.add(room2);

        // 유저가 두 방에 모두 참여 중
        when(roomRepository.findRoomsByPlayerId(2L)).thenReturn(rooms);
        when(memberRepository.findById(2L)).thenReturn(Optional.of(testMembers.get(0)));

        when(room1.hasPlayer(2L)).thenReturn(true);
        when(room2.hasPlayer(2L)).thenReturn(true);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room1));
        when(roomRepository.findById(2L)).thenReturn(Optional.of(room2));

        // 락 획득은 첫 번째 방만 성공, 두 번째 방은 실패 시뮬레이션
        when(redisLockService.acquireLock(eq("lock:room:1"), anyLong(), anyLong())).thenReturn(true);
        when(redisLockService.acquireLock(eq("lock:room:2"), anyLong(), anyLong())).thenReturn(false);

        // when
        roomService.handleDisconnect(2L);

        // then
        // 첫 번째 방에서는 플레이어 제거됨
        verify(room1).removePlayer(2L);

        // 두 번째 방은 락 획득 실패로 플레이어 제거되지 않음
        verify(room2, never()).removePlayer(2L);
    }

    @Test
    @DisplayName("준비 상태 변경 충돌 테스트")
    void toggleReadyConcurrencyTest() {
        // given
        Room spyRoom = spy(testRoom);
        Set<Long> readyPlayers = new HashSet<>();
        ReflectionTestUtils.setField(spyRoom, "readyPlayers", readyPlayers);

        when(roomRepository.findRoomById(1L)).thenReturn(Optional.of(spyRoom));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(testMembers.get(0)));
        when(spyRoom.hasPlayer(2L)).thenReturn(true);

        // 첫 번째 호출에서는 준비 안됨, 두 번째 호출에서는 이미 준비됨 시뮬레이션
        when(spyRoom.getReadyPlayers())
                .thenReturn(new HashSet<>())  // 첫 번째 호출: 빈 셋
                .thenReturn(Set.of(2L));      // 두 번째 호출: ID=2 포함된 셋

        // when
        // 첫 번째 준비 상태 변경 (준비 -> 준비 완료)
        roomService.toggleReady(1L, 2L);

        // 두 번째 준비 상태 변경 (준비 완료 -> 준비 해제)
        roomService.toggleReady(1L, 2L);

        // then
        // 준비 상태 변경 메소드가 각각 한 번씩 호출됨
        verify(spyRoom, times(1)).playerReady(2L);    // 첫 번째 호출
        verify(spyRoom, times(1)).playerUnready(2L);  // 두 번째 호출
    }

    @Test
    @DisplayName("방 입장과 블랙리스트 추가 경쟁 테스트")
    void joinRoomWithBlacklistAddConcurrencyTest() {
        // given
        Room spyRoom = spy(testRoom);

        when(roomRepository.findRoomById(1L)).thenReturn(Optional.of(spyRoom));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(testMembers.get(0)));

        // 첫 번째 호출에서는 블랙리스트에 없음, 두 번째 호출에서는 블랙리스트에 있음 시뮬레이션
        when(blacklistService.isBlacklisted(1L, 2L))
                .thenReturn(false)  // 첫 번째 호출: 블랙리스트 아님
                .thenReturn(true);  // 두 번째 호출: 블랙리스트에 추가됨

        when(spyRoom.validatePassword(any())).thenReturn(true);
        when(spyRoom.isFull()).thenReturn(false);
        when(spyRoom.hasPlayer(2L)).thenReturn(false);

        // when & then
        // 첫 번째 입장 시도: 성공 (아직 블랙리스트에 없음)
        roomService.joinRoom(1L, 2L, null);

        // 두 번째 입장 시도: 실패 (블랙리스트에 추가됨)
        assertThatThrownBy(() -> roomService.joinRoom(1L, 2L, null))
                .isInstanceOf(ServiceException.class);

        // 방 입장은 첫 번째만 성공
        verify(spyRoom, times(1)).addPlayer(2L);
    }
}