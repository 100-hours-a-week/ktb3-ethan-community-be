package org.restapi.springrestapi.exception;


import jakarta.servlet.http.HttpServletRequest;
import org.restapi.springrestapi.exception.code.CommonErrorCode;
import org.restapi.springrestapi.exception.code.ErrorCode;
import org.restapi.springrestapi.common.APIResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<APIResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		log.warn("Validation failed: {}", e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(APIResponse.error(
				e.getBindingResult()
					.getAllErrors()
					.get(0)
					.getDefaultMessage()
			));
	}

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<APIResponse<?>> handleBadRequest(Exception e, HttpServletRequest request) {
        log.warn("[400] {} {} - {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.error(CommonErrorCode.BAD_REQUEST));
    }

    // 404: 라우팅 자체가 없음
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<APIResponse<?>> handleNotFound(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("[404] {} {} - {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(APIResponse.error(CommonErrorCode.NOT_FOUND));
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<APIResponse<?>> handleAppException(AppException e, HttpServletRequest request) {
        ErrorCode code = e.getErrorCode();
        log.warn("[AppException {}] {} {} - {}", code.getStatus(), request.getMethod(), request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(code.getStatus())
                .body(APIResponse.error(code));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<?>> handleAnyException(Exception e, HttpServletRequest request) {
        log.error("[500] {} {} - {}", request.getMethod(), request.getRequestURI(), e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(APIResponse.error(CommonErrorCode.INTERNAL));
    }
}