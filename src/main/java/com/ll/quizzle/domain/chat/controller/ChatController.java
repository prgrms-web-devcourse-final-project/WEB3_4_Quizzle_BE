package com.ll.quizzle.domain.chat.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import com.ll.quizzle.domain.chat.dto.ChatMessageDto;
import com.ll.quizzle.domain.chat.service.ChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {
	private final ChatService chatMessageService;
	private final SimpMessagingTemplate messagingTemplate;
	private final ChatService chatService;

	/**
	 * 클라이언트가 보낸 채팅 메시지를 수신하고, 저장 후 전체에게 전송
	 */
	@MessageMapping("/chat.send")  // → 클라이언트에서 /app/chat.send로 보냄
	public void sendMessage(ChatMessageDto chatMessageDto) {
		// 메시지 저장
		ChatMessageDto messageSaved = chatService.save(chatMessageDto);

		// 저장된 메시지를 모든 구독자에게 broadcast
		messagingTemplate.convertAndSend("/topic/chat/room", messageSaved);
	}
}
