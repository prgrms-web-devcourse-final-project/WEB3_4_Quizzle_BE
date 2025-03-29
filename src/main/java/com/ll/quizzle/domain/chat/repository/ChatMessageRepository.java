package com.ll.quizzle.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ll.quizzle.domain.chat.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}
