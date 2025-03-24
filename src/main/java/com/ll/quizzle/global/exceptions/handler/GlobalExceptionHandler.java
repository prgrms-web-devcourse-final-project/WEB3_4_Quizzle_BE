package com.ll.quizzle.global.exceptions.handler;

import com.ll.quizzle.global.exceptions.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<String> handle(ServiceException ex) {

        String message = ex.getMsg();

        return ResponseEntity
                .status(ex.getResultCode())
                .body(message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        StringBuilder errorMessages = new StringBuilder();
        for (FieldError fieldError : fieldErrors) {
            errorMessages.append(fieldError.getField())
                    .append(": ")
                    .append(fieldError.getDefaultMessage())
                    .append("\n");
        }

        return ResponseEntity.badRequest().body(errorMessages.toString());
    }

}
