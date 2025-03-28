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
import com.ll.quizzle.standard.page.dto.OnlineUsersPageDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebSocketEventListener {
	private final MessageService messageService;
	private final MemberService memberService;
	private final Map<String, String> sessionUserMapping = new ConcurrentHashMap<>();
	private static final int INITIAL_PAGE_SIZE = 30;
	private static final int SCROLL_PAGE_SIZE = 10;

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

		// 처음 접속했을 때는 30명만 보여주기
		sendPagedUserList(onlineUsers, 0, INITIAL_PAGE_SIZE);
	}

	public void loadMoreUsers(int page) {
		Set<String> uniqueEmails = new HashSet<>(sessionUserMapping.values());
		List<MemberDto> onlineUsers = memberService.getOnlineUsers(uniqueEmails);
		sendPagedUserList(onlineUsers, page, SCROLL_PAGE_SIZE);
	}

	private void sendPagedUserList(List<MemberDto> allMembers, int page, int pageSize) {
		// OnlineUsersPageDto.from() 메서드를 사용하여 페이징 처리
		OnlineUsersPageDto response = OnlineUsersPageDto.from(allMembers, page, pageSize);

		messageService.send("/topic/lobby/users", response);

		log.debug("온라인 사용자 목록 전송 - 페이지: {}, 크기: {}, 전체: {}",
			response.getCurrentPageNumber(),
			response.getPageSize(),
			response.getTotalItems());
	}
}
