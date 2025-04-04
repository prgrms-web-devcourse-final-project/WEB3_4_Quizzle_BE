package com.ll.quizzle.global.socket.session;

import com.ll.quizzle.global.socket.core.SessionInfo;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 기존 인메모리 방식의 세션 관리 방식의 세션 공유 문제를 해결하기 위해
 * Redis 방식을 도입하면서 인터페이스를 분리, 추후 확장성도 고려
 */
public interface WebSocketSessionManager {


    void registerSession(String email, String sessionId, String accessToken, Long expiryTime);


    void removeSession(String email, String sessionId);


    boolean isSessionValid(String email, String sessionId);


    void removeExpiredSessions(long currentTime, BiConsumer<String, SessionInfo> expiredSessionCallback);


    Map<String, Map<String, SessionInfo>> getActiveUserSessions();
} 