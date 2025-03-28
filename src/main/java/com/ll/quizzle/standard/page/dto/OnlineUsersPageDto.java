package com.ll.quizzle.standard.page.dto;

import java.util.List;

import com.ll.quizzle.domain.member.dto.MemberDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnlineUsersPageDto {
	private PageDto<MemberDto> pageInfo;
	private boolean hasMore;

	public static OnlineUsersPageDto from(List<MemberDto> allMembers, int currentPage, int pageSize) {
		int start = currentPage * pageSize;
		int end = Math.min(start + pageSize, allMembers.size());

		PageDto<MemberDto> pageDto = new PageDto<>(
			currentPage + 1,
			pageSize,
			(long)Math.ceil((double)allMembers.size() / pageSize),
			allMembers.size(),
			allMembers.subList(start, end)
		);

		return OnlineUsersPageDto.builder()
			.pageInfo(pageDto)
			.hasMore(end < allMembers.size())
			.build();
	}

	// 편의 메서드들
	public int getCurrentPageNumber() {
		return pageInfo.currentPageNumber();
	}

	public int getPageSize() {
		return pageInfo.pageSize();
	}

	public long getTotalPages() {
		return pageInfo.totalPages();
	}

	public long getTotalItems() {
		return pageInfo.totalItems();
	}

	public List<MemberDto> getItems() {
		return pageInfo.items();
	}
}
