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
		if (allMembers == null || allMembers.isEmpty()) {
			return createEmptyResponse(currentPage, pageSize);
		}

		int start;
		int effectivePageSize = pageSize;

		if (currentPage == 0) {
			// 첫 페이지는 0부터 30개
			start = 0;
			effectivePageSize = 30;  // INITIAL_PAGE_SIZE
		} else {
			// 그 다음부터는 30 + ((페이지-1) * 10) 부터 시작
			start = 30 + ((currentPage - 1) * 10);  // 30 + ((page-1) * SCROLL_PAGE_SIZE)
			effectivePageSize = 10;  // SCROLL_PAGE_SIZE
		}

		if (start >= allMembers.size()) {
			return createEmptyResponse(currentPage, effectivePageSize);
		}

		int end = Math.min(start + effectivePageSize, allMembers.size());

		// 총 페이지 수 계산 수정
		long totalPages = currentPage == 0 ?
			1 + (long)Math.ceil((double)(allMembers.size() - 30) / 10) :
			(long)Math.ceil((double)(allMembers.size() - 30) / 10) + 1;

		PageDto<MemberDto> pageDto = new PageDto<>(
			currentPage + 1,
			effectivePageSize,
			totalPages,
			allMembers.size(),
			allMembers.subList(start, end)
		);

		return OnlineUsersPageDto.builder()
			.pageInfo(pageDto)
			.hasMore(end < allMembers.size())
			.build();
	}

	private static OnlineUsersPageDto createEmptyResponse(int currentPage, int pageSize) {
		PageDto<MemberDto> emptyPage = new PageDto<>(
			currentPage + 1,
			pageSize,
			0L,
			0L,
			List.of()
		);

		return OnlineUsersPageDto.builder()
			.pageInfo(emptyPage)
			.hasMore(false)
			.build();
	}

	// 기존 편의 메서드들은 유지
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
