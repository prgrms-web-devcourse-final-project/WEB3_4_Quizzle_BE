package com.ll.quizzle.domain.chat.service;

import org.springframework.stereotype.Service;

import com.ll.quizzle.domain.chat.dto.ChatMessageDto;
import com.ll.quizzle.domain.chat.entity.ChatMessage;
import com.ll.quizzle.domain.chat.repository.ChatMessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {
	private final ChatMessageRepository chatMessageRepository;

	/**
	 * 채팅 메시지를 저장하고, 저장된 메시지를 Dto 로 반환
	 */
	public ChatMessageDto save(ChatMessageDto chatMessageDto) {
		// DTO → 엔티티로 변환
		ChatMessage chatMessage = new ChatMessage(
			chatMessageDto.sender(),
			chatMessageDto.message(),
			chatMessageDto.type()
		);

		// DB에 저장
		ChatMessage memberChatSaved = chatMessageRepository.save(chatMessage);

		// 저장된 엔티티 → 다시 Dto 로 변환
		return ChatMessageDto.from(memberChatSaved);
	}
}
