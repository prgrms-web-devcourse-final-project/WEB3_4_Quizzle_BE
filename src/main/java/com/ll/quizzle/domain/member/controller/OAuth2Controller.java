package com.ll.quizzle.domain.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ll.quizzle.domain.member.dto.OAuth2Response;
import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.global.request.Rq;
import com.ll.quizzle.global.response.RsData;
import com.ll.quizzle.global.security.oauth2.entity.OAuth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class OAuth2Controller {
	private final Rq rq;

	/**
	 * Oauth2 callback
	 *
	 * @param accessToken  the access token
	 * @param refreshToken the refresh token
	 * @param status       the status (SUCCESS)
	 *
	 * @return the rs data
	 */
	@GetMapping("/oauth2/callback") // oauth 정보 확인용 (테스트를 위해 일단 보류)
	public RsData<OAuth2Response> callback(
		@RequestParam(required = false) String accessToken,
		@RequestParam(required = false) String refreshToken,
		@RequestParam String status
	) {
		if (!"SUCCESS".equals(status)) {
			return RsData.success(HttpStatus.BAD_REQUEST, new OAuth2Response(
				null, null, status, null, null, null,
				false, false, null, null
			));
		}

		Member actor = rq.getActor();
		if (actor == null) {
			return RsData.success(HttpStatus.UNAUTHORIZED, new OAuth2Response(
				null, null, status, null, null, null,
				false, false, null, null
			));
		}

		OAuth oAuth = actor.getFirstOAuth();
		if (oAuth == null) {
			return RsData.success(HttpStatus.BAD_REQUEST, new OAuth2Response(
				actor.getEmail(), actor.getNickname(), status,
				actor.getUserRole(), null, null,
				actor.isMember(), actor.isAdmin(),
				accessToken, refreshToken
			));
		}

		OAuth2Response response = new OAuth2Response(
			actor.getEmail(),
			actor.getNickname(),
			status,
			actor.getUserRole(),
			oAuth.getProvider(),
			oAuth.getOauthId(),
			actor.isMember(),
			actor.isAdmin(),
			accessToken,
			refreshToken
		);

		return RsData.success(HttpStatus.OK, response);
	}
}
