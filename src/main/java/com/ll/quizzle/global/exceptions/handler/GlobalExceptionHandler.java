package com.ll.quizzle.global.exceptions.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.ll.quizzle.global.exceptions.ErrorCode;
import com.ll.quizzle.global.exceptions.ServiceException;
import com.ll.quizzle.global.response.RsData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<RsData<?>> handle(ServiceException ex) {
        HttpStatus status = ex.getResultCode() != null ? ex.getResultCode() : ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus();
        String message = ex.getMsg() != null ? ex.getMsg() : ErrorCode.INTERNAL_SERVER_ERROR.getMessage();
        
        return ResponseEntity
                .status(status)
                .body(new RsData<>(status, message, null));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<RsData<?>> handleValidationException(MethodArgumentNotValidException ex) {
        StringBuilder errorMessages = new StringBuilder();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorMessages.append(fieldError.getField())
                    .append(": ")
                    .append(fieldError.getDefaultMessage())
                    .append("\n");
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new RsData<>(HttpStatus.BAD_REQUEST, errorMessages.toString(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RsData<?>> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(new RsData<>(
                    ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus(),
                    ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                    null
                ));
    }
}