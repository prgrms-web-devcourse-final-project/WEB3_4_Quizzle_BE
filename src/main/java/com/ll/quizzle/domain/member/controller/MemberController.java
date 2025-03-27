package com.ll.quizzle.domain.member.controller;

import com.ll.quizzle.domain.member.service.MemberService;
import com.ll.quizzle.global.response.RsData;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.ll.quizzle.global.exceptions.ErrorCode.REFRESH_TOKEN_NOT_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {
    private final MemberService memberService;

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        memberService.logout(request, response);
    }

    @PostMapping("/refresh")
    public RsData<String> refresh(HttpServletRequest request) {
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                refreshToken = authHeader.substring(7);
            }
        }

        if (refreshToken == null) {
            REFRESH_TOKEN_NOT_FOUND.throwServiceException();
        }

        return memberService.refreshAccessToken(refreshToken);
    }
}
