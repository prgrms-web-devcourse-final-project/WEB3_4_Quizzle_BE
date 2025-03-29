package com.ll.quizzle.domain.chat.dto;

import com.ll.quizzle.domain.chat.entity.ChatMessage;
import com.ll.quizzle.domain.chat.entity.MessageType;

public record ChatMessageDto(
	String sender,
	String message,
	MessageType type
) {
	public static ChatMessageDto from(ChatMessage message) {
		return new ChatMessageDto(
			message.getSender(),
			message.getMessage(),
			message.getType()
		);
	}
}
