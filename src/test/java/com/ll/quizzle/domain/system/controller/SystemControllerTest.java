package com.ll.quizzle.domain.system.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.quizzle.domain.system.dto.request.SystemLoginRequest;
import com.ll.quizzle.global.config.SystemProperties;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("시스템 ControllerTest")
public class SystemControllerTest {
	@Autowired
	private MockMvc mvc;

	@Autowired
	private SystemProperties systemProperties;
	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("로그인 성공")
	void loginSuccess() throws Exception {
		SystemLoginRequest loginRequest = new SystemLoginRequest(
			systemProperties.getSystemEmail(),
			"system1234"
		);

		mockMvc.perform(post("/api/v1/system/login")
				.content(MediaType.APPLICATION_JSON_VALUE)
				.content(new ObjectMapper().writeValueAsString(loginRequest)))
			.andExpect(status().isOk())
			.andExpect(cookie().exists("access_token"))
			.andExpect(cookie().exists("refresh_token"))
			.andExpect(jsonPath("$.data.role").value("ROLE_SYSTEM"));
	}

	@Test
	@DisplayName("로그인 실패")
	void loginFail() throws Exception {
		SystemLoginRequest loginRequest = new SystemLoginRequest(
			"wrongemail@quizzle.com",
			"system1234"
		);

		mockMvc.perform(post("/api/v1/system/login")
			.content(MediaType.APPLICATION_JSON_VALUE)
			.content(new ObjectMapper().writeValueAsString(loginRequest)))
		.andExpect(status().isUnauthorized());
	}
}
