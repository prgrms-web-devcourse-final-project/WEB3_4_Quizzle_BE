package com.ll.quizzle.global.socket.session;

import com.ll.quizzle.global.socket.core.SessionInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * 기존 메모리 기반 WebSocket 세션 관리 구현체 (Redis 방식 도입 시 사용 안할 예정)
 */
@Slf4j
@Service("inMemoryWebSocketSessionManager")
@RequiredArgsConstructor
public class InMemoryWebSocketSessionManager implements WebSocketSessionManager {

    private final Map<String, Map<String, SessionInfo>> activeUserSessions = new ConcurrentHashMap<>();

    @Override
    public void registerSession(String email, String sessionId, String accessToken, Long expiryTime) {
        Map<String, SessionInfo> sessions = activeUserSessions.computeIfAbsent(email, k -> new ConcurrentHashMap<>());
        sessions.put(sessionId, new SessionInfo(accessToken, expiryTime, sessionId));
        log.debug("세션 등록: 사용자={}, 세션={}", email, sessionId);
    }

    @Override
    public void removeSession(String email, String sessionId) {
        Map<String, SessionInfo> sessions = activeUserSessions.get(email);
        if (sessions != null) {
            sessions.remove(sessionId);
            log.debug("세션 제거: 사용자={}, 세션={}", email, sessionId);
            
            if (sessions.isEmpty()) {
                activeUserSessions.remove(email);
                log.debug("사용자 세션 항목 제거: 사용자={}", email);
            }
        }
    }

    @Override
    public boolean isSessionValid(String email, String sessionId) {
        Map<String, SessionInfo> sessions = activeUserSessions.get(email);
        if (sessions == null) return false;
        
        SessionInfo sessionInfo = sessions.get(sessionId);
        if (sessionInfo == null) return false;
        
        return System.currentTimeMillis() < sessionInfo.expiryTime();
    }

    @Override
    public void removeExpiredSessions(long currentTime, BiConsumer<String, SessionInfo> expiredSessionCallback) {
        activeUserSessions.forEach((email, sessions) -> {
            sessions.entrySet().removeIf(entry -> {
                SessionInfo sessionInfo = entry.getValue();
                if (sessionInfo.expiryTime() < currentTime) {
                    log.debug("토큰 만료 감지 - 사용자: {}, 세션: {}", email, sessionInfo.sessionId());
                    
                    if (expiredSessionCallback != null) {
                        expiredSessionCallback.accept(email, sessionInfo);
                    }
                    
                    return true;
                }
                return false;
            });
            
            if (sessions.isEmpty()) {
                activeUserSessions.remove(email);
            }
        });
    }

    @Override
    public Map<String, Map<String, SessionInfo>> getActiveUserSessions() {
        return activeUserSessions;
    }
} 