package com.ll.quizzle.domain.system.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.domain.member.service.RefreshTokenService;
import com.ll.quizzle.domain.member.type.Role;
import com.ll.quizzle.domain.system.dto.request.RoleChangeRequest;
import com.ll.quizzle.domain.system.dto.request.SystemLoginRequest;
import com.ll.quizzle.domain.system.dto.response.SystemLoginResponse;
import com.ll.quizzle.domain.system.entity.RoleChangeHistory;
import com.ll.quizzle.domain.system.repository.RoleChangeHistoryRepository;
import com.ll.quizzle.global.response.RsData;

@SpringBootTest
public class SystemServiceTest {
	@Autowired
	private SystemService systemService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private RoleChangeHistoryRepository roleChangeHistoryRepository;

	@Autowired
	private RefreshTokenService refreshTokenService;

	@Test
	@DisplayName("시스템 관리자 인증")
	void authenticateSuccess() {
		// Given
		SystemLoginRequest request = new SystemLoginRequest(
			"system@quizzle.com",
			"system1234"
		);
		MockHttpServletResponse response = new MockHttpServletResponse();

		// When
		RsData<SystemLoginResponse> result = systemService.authenticate(request, response);

		// Then
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.getData().role()).isEqualTo("ROLE_SYSTEM");
	}

	@Test
	@DisplayName("권한 변경: member -> admin")
	void changeRoleMemberToAdmin() {
		// Given
		Member member = memberRepository.findByEmail("admin@quizzle.com")
			.orElseThrow();

		RoleChangeRequest request = new RoleChangeRequest(
			"admin@quizzle.com",
			Role.ADMIN,
			"관리자 권한 부여",
			"system@quizzle.com"
		);

		// When
		RsData<Void> result = systemService.changeRole(request);

		// Then
		assertThat(result.isSuccess()).isTrue();
		assertThat(member.getRole()).isEqualTo(Role.ADMIN);

		RoleChangeHistory history = roleChangeHistoryRepository
			.findTopByMemberOrderByCreateDateDesc(member)
			.orElseThrow();
		assertThat(history.getNewRole()).isEqualTo(Role.ADMIN);
	}

	@Test
	@DisplayName("권한 변경: admin -> member")
	void changeRoleAdminToMember() {
		// Given
		Member member = memberRepository.findByEmail("admin@quizzle.com")
			.orElseThrow();

		RoleChangeRequest request = new RoleChangeRequest(
			"member@quizzle.com",
			Role.MEMBER,
			"관리자 권한 회수",
			"system@quizzle.com"
		);

		// When
		RsData<Void> result = systemService.changeRole(request);

		// Then
		assertThat(result.isSuccess()).isTrue();
		assertThat(member.getRole()).isEqualTo(Role.MEMBER);

		var history = roleChangeHistoryRepository
			.findTopByMemberOrderByCreateDateDesc(member)
			.orElseThrow();
		assertThat(history.getNewRole()).isEqualTo(Role.MEMBER);
	}
}
