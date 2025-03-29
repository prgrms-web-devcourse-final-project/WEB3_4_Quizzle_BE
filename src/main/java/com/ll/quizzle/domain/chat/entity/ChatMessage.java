package com.ll.quizzle.domain.chat.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String sender;

	private String message;

	@Enumerated(EnumType.STRING)
	private MessageType type;

	private LocalDateTime createdAt;

	// 생성자
	public ChatMessage(String sender, String message, MessageType type) {
		this.sender = sender;
		this.message = message;
		this.type = type;
		this.createdAt = LocalDateTime.now();
	}
}
