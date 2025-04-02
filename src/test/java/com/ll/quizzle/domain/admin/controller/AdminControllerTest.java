package com.ll.quizzle.domain.admin.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.quizzle.domain.admin.dto.AdminLoginRequestDTO;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${admin.email}")
	private String adminEmail;

	@Value("${admin.password-hash}")
	private String adminPasswordHash;

	@Test
	@DisplayName("관리자 로그인 성공")
	void loginSuccess() throws Exception {
		// given
		AdminLoginRequestDTO request = new AdminLoginRequestDTO(
			adminEmail,        // application-test.yml에서 주입받은 값
			adminPasswordHash  // application-test.yml에서 주입받은 값
		);

		// when
		ResultActions resultActions = mockMvc
			.perform(post("/api/v1/admin/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print());

		// then
		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.resultCode").value("OK"))
			.andExpect(jsonPath("$.data").value("로그인에 성공했습니다."));
	}

	@Test
	@DisplayName("관리자 로그인 실패 - 잘못된 비밀번호")
	void loginFailWrongPassword() throws Exception {
		// given
		AdminLoginRequestDTO request = new AdminLoginRequestDTO(
			adminEmail,
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
			.andExpect(status().isBadRequest())
			.andExpect(result -> assertTrue(result.getResolvedException() instanceof BadCredentialsException))
			.andExpect(result -> assertEquals(
				"아이디 또는 비밀번호가 일치하지 않습니다.",
				result.getResolvedException().getMessage()
			));
	}
}
