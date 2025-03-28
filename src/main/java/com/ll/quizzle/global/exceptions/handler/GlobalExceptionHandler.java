package com.ll.quizzle.global.exceptions.handler;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.ll.quizzle.global.exceptions.ServiceException;
import com.ll.quizzle.global.response.ErrorResponseDTO;
import com.ll.quizzle.global.response.RsData;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

//
//@ControllerAdvice
//@RequiredArgsConstructor
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(ServiceException.class)
//    public ResponseEntity<String> handle(ServiceException ex) {
//
//        String message = ex.getMsg();
//
//        return ResponseEntity
//                .status(ex.getResultCode())
//                .body(message);
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
//        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
//
//        StringBuilder errorMessages = new StringBuilder();
//        for (FieldError fieldError : fieldErrors) {
//            errorMessages.append(fieldError.getField())
//                    .append(": ")
//                    .append(fieldError.getDefaultMessage())
//                    .append("\n");
//        }
//
//        return ResponseEntity.badRequest().body(errorMessages.toString());
//    }
//
//}
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	@ExceptionHandler(ServiceException.class)
	public ResponseEntity<RsData<ErrorResponseDTO>> handle(ServiceException ex, HttpServletRequest req) {
		ErrorResponseDTO errorData = new ErrorResponseDTO(
			req.getRequestURI(),
			LocalDateTime.now()
		);

		return ResponseEntity
			.status(ex.getResultCode())
			.body(new RsData<>(ex.getResultCode(), ex.getMsg(), errorData)
			);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<RsData<ErrorResponseDTO>> handleValidationException(MethodArgumentNotValidException ex,
		HttpServletRequest req) {
		StringBuilder errorMessages = new StringBuilder();
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			errorMessages.append(fieldError.getField())
				.append(": ")
				.append(fieldError.getDefaultMessage())
				.append("\n");
		}

		ErrorResponseDTO errorData = new ErrorResponseDTO(
			req.getRequestURI(),
			LocalDateTime.now()
		);

		return ResponseEntity
			.badRequest()
			.body(new RsData<>(HttpStatus.BAD_REQUEST, errorMessages.toString(), errorData));
	}

}