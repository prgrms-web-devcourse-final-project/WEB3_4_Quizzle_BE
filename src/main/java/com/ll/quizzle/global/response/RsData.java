package com.ll.quizzle.global.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

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

    @JsonIgnore
    public boolean isSuccess() { return !resultCode.isError(); }

    @JsonIgnore
    public boolean isFail() {
        return resultCode.isError();
    }

    public static <T> RsData<T> success(HttpStatus resultCode, T data) {
        return new RsData<>(resultCode, "OK", data);
    }
}