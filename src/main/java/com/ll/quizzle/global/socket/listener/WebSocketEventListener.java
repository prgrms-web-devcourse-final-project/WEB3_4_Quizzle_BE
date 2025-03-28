package com.ll.quizzle.global.socket.listener;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.ll.quizzle.domain.member.dto.MemberDto;
import com.ll.quizzle.domain.member.service.MemberService;
import com.ll.quizzle.global.socket.core.MessageService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebSocketEventListener {
	private final MessageService messageService;
	private final MemberService memberService;
	private final Map<String, String> sessionUserMapping = new ConcurrentHashMap<>();

	public WebSocketEventListener(
		@Qualifier("stompMessageService") MessageService messageService,
		MemberService memberService
	) {
		this.messageService = messageService;
		this.memberService = memberService;
	}

	@EventListener
	public void handleWebSocketConnectListener(SessionConnectedEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		Principal user = headerAccessor.getUser();

		if (user == null) {
			log.warn("웹소켓 연결 시 사용자 정보를 찾을 수 없습니다.");
			return;
		}

		String email = user.getName();
		String sessionId = headerAccessor.getSessionId();

		sessionUserMapping.put(sessionId, email);
		log.info("사용자 연결 완료: {} (세션 ID: {})", email, sessionId);

		broadcastUserList();
	}

	@EventListener
	public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = headerAccessor.getSessionId();

		if (sessionUserMapping.containsKey(sessionId)) {
			String email = sessionUserMapping.remove(sessionId);
			log.info("사용자 연결 해제: {} (세션 ID: {})", email, sessionId);
			broadcastUserList();
		} else {
			log.warn("사용자 연결 해제 중 세션 ID {} 를 찾을 수 없습니다. ", sessionId);
		}
	}

	private void broadcastUserList() {
		Set<String> uniqueEmails = new HashSet<>(sessionUserMapping.values());
		List<MemberDto> onlineUsers = memberService.getOnlineUsers(uniqueEmails);
		messageService.send("/topic/lobby/users", onlineUsers);
	}
}
