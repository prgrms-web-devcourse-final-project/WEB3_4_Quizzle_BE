package com.ll.quizzle.domain.system.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.quizzle.domain.system.dto.request.SystemLoginRequest;
import com.ll.quizzle.global.config.SystemProperties;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("시스템 ControllerTest")
public class SystemControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private SystemProperties systemProperties;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		// SystemProperties에 테스트용 해시 비밀번호 설정
		ReflectionTestUtils.setField(systemProperties, "systemPasswordHash",
			passwordEncoder.encode("system1234"));
		ReflectionTestUtils.setField(systemProperties, "secondaryPasswordHash",
			passwordEncoder.encode("system5678"));
	}

	@Test
	@DisplayName("로그인 성공")
	void loginSuccess() throws Exception {
		// Given
		SystemLoginRequest loginRequest = new SystemLoginRequest(
			systemProperties.getSystemEmail(),
			"system1234",
			"system5678"
		);

		// When & Then
		mockMvc.perform(post("/api/v1/system/login")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(loginRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.resultCode").value("OK"))
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.role").value("ROLE_SYSTEM"))
			.andExpect(cookie().exists("access_token"))
			.andExpect(cookie().exists("refresh_token"));
	}

	@Test
	@DisplayName("로그인 실패 - 잘못된 이메일")
	void loginFail() throws Exception {
		SystemLoginRequest loginRequest = new SystemLoginRequest(
			"wrongemail@quizzle.com",
			"system1234",
			"system5678"
		);

		mockMvc.perform(post("/api/v1/system/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.resultCode").value("UNAUTHORIZED"));
	}

	@Test
	@DisplayName("로그인 실패 - 잘못된 2차 비밀번호")
	void loginFailWithWrongSecondaryPassword() throws Exception {
		SystemLoginRequest loginRequest = new SystemLoginRequest(
			systemProperties.getSystemEmail(),
			"system1234",
			"wrongSecondaryPassword"
		);

		mockMvc.perform(post("/api/v1/system/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.resultCode").value("UNAUTHORIZED"))
			.andExpect(cookie().doesNotExist("access_token"))
			.andExpect(cookie().doesNotExist("refresh_token"));
	}
}
