package com.ll.quizzle.global.exceptions;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@AllArgsConstructor
@Slf4j
public enum ErrorCode {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

	// point
	POINT_INCREASE_AMOUNT_INVALID(HttpStatus.BAD_REQUEST, "증가 포인트는 0보다 커야 합니다."),
	POINT_DECREASE_AMOUNT_INVALID(HttpStatus.BAD_REQUEST, "차감 포인트는 0보다 커야 합니다."),
	POINT_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "포인트가 부족합니다."),
	INVALID_POINT_REASON(HttpStatus.BAD_REQUEST, "유효하지 않은 포인트 사유입니다."),
	POINT_POLICY_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "포인트 정책이 정의되지 않았습니다."),

    // jwt
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_LOGGED_OUT(HttpStatus.UNAUTHORIZED, "로그아웃된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "리프레시 토큰을 찾을 수 없습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 리프레시 토큰입니다."),
	TOKEN_INFO_NOT_FOUND(HttpStatus.BAD_REQUEST, "토큰 정보가 없습니다."),

    // member + oauth
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원이 존재하지 않습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다."),
    OAUTH_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 OAuth 정보를 찾을 수 없습니다."),
    OAUTH_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "OAuth 로그인에 실패했습니다."),

    // WebSocket
    WEBSOCKET_COOKIE_NOT_FOUND(HttpStatus.UNAUTHORIZED, "WebSocket 연결을 위한 쿠키가 없습니다."),
    WEBSOCKET_ACCESS_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "WebSocket 연결을 위한 액세스 토큰이 없습니다."),
    WEBSOCKET_TOKEN_VALIDATION_FAILED(HttpStatus.UNAUTHORIZED, "WebSocket 토큰 검증에 실패했습니다."),
    WEBSOCKET_INVALID_REQUEST_TYPE(HttpStatus.BAD_REQUEST, "잘못된 WebSocket 요청 타입입니다."),
    WEBSOCKET_UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 메시징 프로바이더입니다."),

    // GameRoom
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 방입니다."),
    NOT_ROOM_OWNER(HttpStatus.FORBIDDEN, "방장만 이 작업을 수행할 수 있습니다."),
    ROOM_IS_FULL(HttpStatus.BAD_REQUEST, "방이 가득 찼습니다."),
    NOT_ALL_PLAYERS_READY(HttpStatus.BAD_REQUEST, "모든 플레이어가 준비되지 않았습니다."),
    GAME_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "이미 게임이 시작되었습니다."),

	// Global
	FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
	INVALID_PAGE_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 페이지 요청입니다.");

    private final HttpStatus httpStatus;
    private final String message;

    public ServiceException throwServiceException() {
        throw new ServiceException(httpStatus, message);
    }

    public ServiceException throwServiceException(Throwable cause) {
        throw new ServiceException(httpStatus, message, cause);
    }
}
