//package com.ll.quizzle.global.socket.dto;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
/// **
// * 방 내부 메시지를 위한 DTO
// * 게임 상태, 준비 상태 등의 메시지에 사용됩니다.
// */
//@Getter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class RoomMessageDTO {
//
//    private MessageType type;
//
//    private String content;
//
//    /**
//     * JSON 형식의 추가 데이터를 전달하기 위해 추가했습니다.
//     * 폴링 없이 실시간으로 데이터를 전달하기 위해 추가했습니다.
//     */
//    private String data;
//
//    private String senderId;
//
//    private String senderName;
//
//    /**
//     * Room 쪽 timestamp 역시 히스토리 관리 및 확장성을 위해 추가했습니다.
//     */
//    private long timestamp;
//
//    private String roomId;
//
//    public enum MessageType {
//        JOIN,           // 방 입장
//        LEAVE,          // 방 퇴장
//        READY,          // 준비 완료
//        UNREADY,        // 준비 취소
//        GAME_START,     // 게임 시작
//        GAME_END,       // 게임 종료
//        ANSWER_SUBMIT,  // 정답 제출
//        TIMER,          // 타이머 업데이트
//        ROUND_START,    // 라운드 시작
//        ROUND_END,      // 라운드 종료
//        SYSTEM          // 시스템 메시지
//    }
//}