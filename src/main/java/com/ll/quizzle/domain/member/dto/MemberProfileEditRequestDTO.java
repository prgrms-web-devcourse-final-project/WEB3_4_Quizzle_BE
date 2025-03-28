package com.ll.quizzle.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberProfileEditRequestDTO(

	@NotBlank(message = "닉네임은 필수입니다.")
	@Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
	String nickname
) {
}
