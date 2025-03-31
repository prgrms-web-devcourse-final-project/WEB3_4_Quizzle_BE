package com.ll.quizzle.domain.point.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ll.quizzle.domain.point.dto.request.PointHistoryRequest;
import com.ll.quizzle.domain.point.dto.response.PointHistoryResponse;
import com.ll.quizzle.domain.point.service.PointService;
import com.ll.quizzle.domain.point.type.PointType;
import com.ll.quizzle.global.response.RsData;
import com.ll.quizzle.standard.page.dto.PageDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class PointController {

	private final PointService pointService;

	@GetMapping("/{memberId}/points")
	public RsData<PageDto<PointHistoryResponse>> getPointHistories(
		@PathVariable("memberId") Long memberId,
		@RequestParam(defaultValue = "ALL") PointType type,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		PointHistoryRequest requestDto = new PointHistoryRequest(type, page, size);
		PageDto<PointHistoryResponse> result = pointService.getPointHistoriesWithPage(memberId, requestDto);
		return RsData.success(HttpStatus.OK, result);
	}

}
