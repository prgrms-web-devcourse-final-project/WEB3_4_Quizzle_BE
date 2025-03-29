package com.ll.quizzle.global.response;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@Getter
public class RsData<T> {
	@lombok.NonNull
	private final HttpStatus resultCode;
	@lombok.NonNull
	private final String msg;
	@NonNull
	private final T data;

	public static <T> RsData<T> of(HttpStatus resultCode, String msg, T data) {
		return new RsData<>(resultCode, msg, data);
	}

	@JsonIgnore
	public boolean isSuccess() {
		return !resultCode.isError();
	}

	@JsonIgnore
	public boolean isFail() {
		return resultCode.isError();
	}

	public static <T> RsData<T> success(HttpStatus resultCode, T data) {
		return new RsData<>(resultCode, "OK", data);
	}

	public static <T> RsData<T> fail(HttpStatus resultCode, String msg, T data) {
		return new RsData<>(resultCode, msg, data);
	}
}
