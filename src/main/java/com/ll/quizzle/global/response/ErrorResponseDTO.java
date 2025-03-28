package com.ll.quizzle.global.response;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
	String path,
	LocalDateTime timestamp
) {
}
