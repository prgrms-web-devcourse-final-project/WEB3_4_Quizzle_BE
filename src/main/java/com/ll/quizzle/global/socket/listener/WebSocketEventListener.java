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
		String sessionId = headerAccessor.getSessionId();

		Principal user = headerAccessor.getUser();
		if (user == null) {
			log.warn("웹소켓 연결 시 사용자 정보를 찾을 수 없습니다.");
			return;
		}

		String email = user.getName();
		sessionUserMapping.put(sessionId, email);
		log.info("사용자 연결 완료: {} (세션 ID: {})", email, sessionId);

		/* // 테스트용 코드 시작
		String testEmail = "test1@email.com";
		sessionUserMapping.put(sessionId, testEmail);
		log.debug("테스트 웹소켓 연결 - 세션: {}, 이메일: {}", sessionId, testEmail);*/

		sendInitialUserList();
	}

	@EventListener
	public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = headerAccessor.getSessionId();

		if (sessionUserMapping.containsKey(sessionId)) {
			String email = sessionUserMapping.remove(sessionId);
			log.info("사용자 연결 해제: {} (세션 ID: {})", email, sessionId);
		} else {
			log.warn("사용자 연결 해제 중 세션 ID {} 를 찾을 수 없습니다. ", sessionId);
		}
	}

	// 공통으로 사용할 전체 사용자 목록 가져오기 메서드
	public List<MemberDto> getAllOnlineUsers() {
		Set<String> uniqueEmails = new HashSet<>(sessionUserMapping.values());
		return memberService.getOnlineUsers(uniqueEmails);
	}

	public void loadMoreUsers(int page) {
		List<MemberDto> onlineUsers = getAllOnlineUsers();
		log.debug("추가 데이터 로드 요청 - 페이지: {}, 전체 사용자 수: {}", page, onlineUsers.size());
		sendPagedUserList(onlineUsers, page, SCROLL_PAGE_SIZE);
	}

	private void sendPagedUserList(List<MemberDto> allMembers, int page, int pageSize) {
		OnlineUsersPageDto response = OnlineUsersPageDto.from(allMembers, page, pageSize);
		messageService.send("/topic/lobby/users", response);

		log.debug("사용자 목록 전송 완료 - 페이지: {}, 페이지 크기: {}, 전체 사용자 수: {}, 현재 페이지 항목 수: {}, 더 있음: {}",
			response.getCurrentPageNumber(),
			response.getPageSize(),
			response.getTotalItems(),
			response.getItems().size(),
			response.isHasMore());
	}

	// 초기 사용자 목록 전송을 위한 별도 메서드
	private void sendInitialUserList() {
		List<MemberDto> onlineUsers = getAllOnlineUsers();
		log.debug("초기 사용자 목록 전송 시작 - 전체 사용자 수: {}", onlineUsers.size());
		sendPagedUserList(onlineUsers, 0, INITIAL_PAGE_SIZE);
	}
}
