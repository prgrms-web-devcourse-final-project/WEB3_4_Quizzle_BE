package com.ll.quizzle.domain.system.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.domain.member.type.Role;
import com.ll.quizzle.domain.system.dto.request.RoleChangeRequest;
import com.ll.quizzle.domain.system.dto.request.SystemLoginRequest;
import com.ll.quizzle.domain.system.dto.response.SystemLoginResponse;
import com.ll.quizzle.domain.system.entity.RoleChangeHistory;
import com.ll.quizzle.domain.system.repository.RoleChangeHistoryRepository;
import com.ll.quizzle.global.config.SystemProperties;
import com.ll.quizzle.global.response.RsData;

@SpringBootTest
@ActiveProfiles("Test")
@Transactional
public class SystemServiceTest {
	@Autowired
	private SystemService systemService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private RoleChangeHistoryRepository roleChangeHistoryRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private SystemProperties systemProperties;

	private static final String SYSTEM_PASSWORD = "system1234";
	private static final String SECONDARY_PASSWORD = "system5678";
	private static final String SYSTEM_EMAIL = "system@quizzle.com";
	private static final String DEFAULT_PROFILE_PATH = "image";

	@BeforeEach
	void setUp() {
		// 기존 데이터 초기화
		memberRepository.deleteAll();
		roleChangeHistoryRepository.deleteAll();

		// SystemProperties 설정
		String encodedSystemPassword = passwordEncoder.encode(SYSTEM_PASSWORD);
		String encodedSecondaryPassword = passwordEncoder.encode(SECONDARY_PASSWORD);

		ReflectionTestUtils.setField(systemProperties, "systemPasswordHash", encodedSystemPassword);
		ReflectionTestUtils.setField(systemProperties, "secondaryPasswordHash", encodedSecondaryPassword);
		ReflectionTestUtils.setField(systemProperties, "systemEmail", SYSTEM_EMAIL);

		// 시스템 계정 생성
		Member systemMember = Member.builder()
			.email(SYSTEM_EMAIL)
			.nickname("System")
			.role(Role.SYSTEM)
			.profilePath(DEFAULT_PROFILE_PATH)  // 프로필 경로 추가
			.exp(0)                           // exp 추가
			.level(1)                         // level 추가
			.pointBalance(0)                  // pointBalance 추가
			.build();
		memberRepository.save(systemMember);

		// 관리자 계정 생성
		Member adminMember = Member.builder()
			.email("admin@quizzle.com")
			.nickname("Admin")
			.role(Role.ADMIN)
			.profilePath(DEFAULT_PROFILE_PATH)  // 프로필 경로 추가
			.exp(0)
			.level(1)
			.pointBalance(0)
			.build();
		memberRepository.save(adminMember);

		// 일반 회원 계정 생성
		Member normalMember = Member.builder()
			.email("member@quizzle.com")
			.nickname("Member")
			.role(Role.MEMBER)
			.profilePath(DEFAULT_PROFILE_PATH)  // 프로필 경로 추가
			.exp(0)
			.level(1)
			.pointBalance(0)
			.build();
		memberRepository.save(normalMember);
	}

	@Test
	@DisplayName("시스템 관리자 인증")
	void authenticateSuccess() {
		// Given
		SystemLoginRequest loginRequest = new SystemLoginRequest(
			SYSTEM_EMAIL,
			SYSTEM_PASSWORD,
			SECONDARY_PASSWORD
		);
		MockHttpServletResponse response = new MockHttpServletResponse();

		// When
		RsData<SystemLoginResponse> result = systemService.authenticate(loginRequest, response);

		// Then
		assertThat(result.isSuccess()).isTrue();
		assertThat(result.getData().role()).isEqualTo("ROLE_SYSTEM");
	}

	@Test
	@DisplayName("권한 변경 성공: member -> admin")
	void changeRoleMemberToAdmin() {
		// Given
		String memberEmail = "member@quizzle.com";
		RoleChangeRequest request = new RoleChangeRequest(
			memberEmail,
			Role.ADMIN,
			"관리자 권한 부여",
			SECONDARY_PASSWORD,
			SYSTEM_EMAIL
		);

		// When
		RsData<Void> result = systemService.changeRole(request);

		// Then
		assertThat(result.isSuccess()).isTrue();
		Member updatedMember = memberRepository.findByEmail(memberEmail).orElseThrow();
		assertThat(updatedMember.getRole()).isEqualTo(Role.ADMIN);

		RoleChangeHistory history = roleChangeHistoryRepository
			.findTopByMemberOrderByCreateDateDesc(updatedMember)
			.orElseThrow();
		assertThat(history.getNewRole()).isEqualTo(Role.ADMIN);
	}

	@Test
	@DisplayName("권한 변경 성공: admin -> member")
	void changeRoleAdminToMember() {
		// Given
		String adminEmail = "admin@quizzle.com";
		RoleChangeRequest request = new RoleChangeRequest(
			adminEmail,
			Role.MEMBER,
			"관리자 권한 회수",
			SECONDARY_PASSWORD,
			SYSTEM_EMAIL
		);

		// When
		RsData<Void> result = systemService.changeRole(request);

		// Then
		assertThat(result.isSuccess()).isTrue();
		Member updatedMember = memberRepository.findByEmail(adminEmail).orElseThrow();
		assertThat(updatedMember.getRole()).isEqualTo(Role.MEMBER);
	}

	@Test
	@DisplayName("권한 변경 실패: 2차 비밀번호 오류)")
	void changeRoleFail() {
		// Given
		String adminEmail = "admin@quizzle.com";
		RoleChangeRequest request = new RoleChangeRequest(
			adminEmail,
			Role.MEMBER,
			"관리자 권한 회수",
			"wrongPassword",
			SYSTEM_EMAIL
		);

		// When
		RsData<Void> result = systemService.changeRole(request);

		// Then
		assertThat(result.isFail()).isTrue();  // 실패 여부 확인
		assertThat(result.getResultCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(result.getMsg()).isEqualTo("2차 비밀번호가 일치하지 않습니다.");

		Member unchangedMember = memberRepository.findByEmail(adminEmail).orElseThrow();
		assertThat(unchangedMember.getRole()).isEqualTo(Role.ADMIN);
	}

	@Test
	@DisplayName("시스템 관리자 인증 실패: 2차 비밀번호 미입력")
	void authenticateFailWithEmptySecondaryPassword() {
		// Given
		SystemLoginRequest loginRequest = new SystemLoginRequest(
			SYSTEM_EMAIL,
			SYSTEM_PASSWORD,
			""  // 빈 2차 비밀번호
		);
		MockHttpServletResponse response = new MockHttpServletResponse();

		// When
		RsData<SystemLoginResponse> result = systemService.authenticate(loginRequest, response);

		// Then
		assertThat(result.isFail()).isTrue();
	}
}
