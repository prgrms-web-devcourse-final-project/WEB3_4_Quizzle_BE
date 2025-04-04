package com.ll.quizzle.domain.admin.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.quizzle.domain.admin.dto.request.AdminLoginRequest;
import com.ll.quizzle.global.config.AdminProperties;
import com.ll.quizzle.global.exceptions.ServiceException;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private AdminProperties adminProperties;

	@Test
	@DisplayName("로그인 전 테스트 값 출력")
	void checkAdminProps() {
		System.out.println("🧪 AdminEmail: " + adminProperties.getAdminEmail());
		System.out.println("🧪 AdminPasswordHash: " + adminProperties.getAdminPasswordHash());
	}


	@Test
	@DisplayName("관리자 로그인 성공")
	void loginSuccess() throws Exception {
		// given
		AdminLoginRequest request = new AdminLoginRequest(
			adminProperties.getAdminEmail(),
			"admin1234"
		);

		// when
		ResultActions resultActions = mockMvc
			.perform(post("/api/v1/admin/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print());

		// then
		resultActions
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("관리자 로그인 실패 - 잘못된 비밀번호")
	void loginFailWrongPassword() throws Exception {
		// given
		AdminLoginRequest request = new AdminLoginRequest(
			adminProperties.getAdminEmail(),
			"$2a$12$wronghashvalue"
		);

		// when
		ResultActions resultActions = mockMvc
			.perform(post("/api/v1/admin/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print());

		// then
		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(result -> assertTrue(result.getResolvedException() instanceof ServiceException))
			.andExpect(result -> assertEquals(
				"401 UNAUTHORIZED : 아이디 또는 비밀번호가 일치하지 않습니다.",
				result.getResolvedException().getMessage()
			));
	}
}
