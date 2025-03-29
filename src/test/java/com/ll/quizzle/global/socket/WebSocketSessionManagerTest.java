package com.ll.quizzle.global.socket;

import com.ll.quizzle.global.socket.session.WebSocketSessionManager;
import com.ll.quizzle.global.socket.session.WebSocketSessionManager.ExpiredSessionCallback;
import com.ll.quizzle.global.socket.session.WebSocketSessionManager.SessionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class WebSocketSessionManagerTest {
    
    private WebSocketSessionManager sessionManager;
    
    @Mock
    private ExpiredSessionCallback callback;
    
    private final String testEmail = "test@example.com";
    private final String testSessionId = "test-session-id";
    private final String testAccessToken = "test-access-token";
    private final long futureExpiryTime = System.currentTimeMillis() + 3600000; // 1시간 후
    private final long pastExpiryTime = System.currentTimeMillis() - 3600000; // 1시간 전
    
    @BeforeEach
    void setUp() {
        sessionManager = new WebSocketSessionManager();
    }
    
    @Test
    @DisplayName("세션 등록 및 조회 테스트")
    void testRegisterAndGetSession() {
        // given
        // when
        sessionManager.registerSession(testEmail, testSessionId, testAccessToken, futureExpiryTime);
        
        // then
        Map<String, Map<String, SessionInfo>> activeSessions = sessionManager.getActiveUserSessions();
        assertThat(activeSessions).isNotEmpty();
        assertThat(activeSessions).containsKey(testEmail);
        
        Map<String, SessionInfo> userSessions = activeSessions.get(testEmail);
        assertThat(userSessions).containsKey(testSessionId);
        
        SessionInfo sessionInfo = userSessions.get(testSessionId);
        assertThat(sessionInfo.getAccessToken()).isEqualTo(testAccessToken);
        assertThat(sessionInfo.getExpiryTime()).isEqualTo(futureExpiryTime);
        assertThat(sessionInfo.getSessionId()).isEqualTo(testSessionId);
    }
    
    @Test
    @DisplayName("세션 삭제 테스트")
    void testRemoveSession() {
        // given
        sessionManager.registerSession(testEmail, testSessionId, testAccessToken, futureExpiryTime);
        
        // when
        sessionManager.removeSession(testEmail, testSessionId);
        
        // then
        Map<String, Map<String, SessionInfo>> activeSessions = sessionManager.getActiveUserSessions();
        assertThat(activeSessions).isEmpty();
    }
    
    @Test
    @DisplayName("유효한 세션 검증 테스트")
    void testIsSessionValid() {
        // given
        sessionManager.registerSession(testEmail, testSessionId, testAccessToken, futureExpiryTime);
        
        // when
        boolean isValid = sessionManager.isSessionValid(testEmail, testSessionId);
        
        // then
        assertThat(isValid).isTrue();
    }
    
    @Test
    @DisplayName("만료된 세션 검증 테스트")
    void testExpiredSession() {
        // given
        sessionManager.registerSession(testEmail, testSessionId, testAccessToken, pastExpiryTime);
        
        // when
        boolean isValid = sessionManager.isSessionValid(testEmail, testSessionId);
        
        // then
        assertThat(isValid).isFalse();
    }
    
    @Test
    @DisplayName("만료된 세션 자동 삭제 테스트")
    void testRemoveExpiredSessions() {
        // given
        sessionManager.registerSession(testEmail, testSessionId, testAccessToken, pastExpiryTime);
        String validSessionId = "valid-session-id";
        sessionManager.registerSession(testEmail, validSessionId, testAccessToken, futureExpiryTime);
        
        // when
        sessionManager.removeExpiredSessions(System.currentTimeMillis(), callback);
        
        // then
        Map<String, Map<String, SessionInfo>> activeSessions = sessionManager.getActiveUserSessions();
        assertThat(activeSessions).isNotEmpty();
        assertThat(activeSessions).containsKey(testEmail);
        
        Map<String, SessionInfo> userSessions = activeSessions.get(testEmail);
        assertThat(userSessions).doesNotContainKey(testSessionId);
        assertThat(userSessions).containsKey(validSessionId);
        
        verify(callback, times(1)).onSessionExpired(eq(testEmail), any(SessionInfo.class));
    }
    
    @Test
    @DisplayName("콜백 함수 실행 테스트")
    void testExpiredSessionCallback() {
        // given
        sessionManager.registerSession(testEmail, testSessionId, testAccessToken, pastExpiryTime);
        
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);
        ExpiredSessionCallback localCallback = (email, sessionInfo) -> {
            assertThat(email).isEqualTo(testEmail);
            assertThat(sessionInfo.getSessionId()).isEqualTo(testSessionId);
            callbackExecuted.set(true);
        };
        
        // when
        sessionManager.removeExpiredSessions(System.currentTimeMillis(), localCallback);
        
        // then
        assertThat(callbackExecuted.get()).isTrue();
    }
    
    @Test
    @DisplayName("다수의 세션 관리 테스트")
    void testMultipleSessionsManagement() {
        // given
        String anotherEmail = "another@example.com";
        String anotherSessionId = "another-session-id";
        
        sessionManager.registerSession(testEmail, testSessionId, testAccessToken, futureExpiryTime);
        sessionManager.registerSession(anotherEmail, anotherSessionId, testAccessToken, futureExpiryTime);
        
        // when
        Map<String, Map<String, SessionInfo>> activeSessions = sessionManager.getActiveUserSessions();
        
        // then
        assertThat(activeSessions).hasSize(2);
        assertThat(activeSessions).containsKeys(testEmail, anotherEmail);
    }
} 