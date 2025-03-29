package com.ll.quizzle.global.socket.service.redis;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.quizzle.standard.page.dto.OnlineUsersPageDto;

import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMessageListener implements MessageListener {

	private final RedisMessageListenerContainer redisMessageListenerContainer;
	private final SimpMessagingTemplate messagingTemplate;
	private final ObjectMapper objectMapper;

	private final List<String> TOPICS = List.of(
		"/topic/lobby/chat",
		"/topic/lobby/users"
	);

	private final List<String> PATTERN_TOPICS = Arrays.asList(
		"/topic/room/chat/*",
		"/topic/game/chat/*"
	);

	@PostConstruct
	public void init() {
		for (String topic : TOPICS) {
			redisMessageListenerContainer.addMessageListener(
				this,
				new ChannelTopic(topic)
			);
			log.debug("Redis 정확한 채널 구독: {}", topic);
		}

		for (String pattern : PATTERN_TOPICS) {
			redisMessageListenerContainer.addMessageListener(
				this,
				new ChannelTopic(pattern)
			);
			log.debug("Redis 패턴 채널 구독: {}", pattern);
		}
	}

	@Override
	public void onMessage(@NonNull Message message, byte[] pattern) {
		try {
			String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
			String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);

			log.debug("Redis 메시지 수신: 채널={}, 원본 메시지={}", channel, messageBody);

			// /topic/lobby/users 채널의 경우 OnlineUsersPageDto로 변환
			if (channel.equals("/topic/lobby/users")) {
				OnlineUsersPageDto usersDto = objectMapper.readValue(messageBody, OnlineUsersPageDto.class);
				messagingTemplate.convertAndSend(channel, usersDto);
			} else {
				messagingTemplate.convertAndSend(channel, messageBody);
			}

			log.debug("STOMP 클라이언트로 메시지 전달 완료: {}", channel);
		} catch (Exception e) {
			log.error("Redis 메시지 처리 중 오류: {}", e.getMessage(), e);
		}
	}
} 
