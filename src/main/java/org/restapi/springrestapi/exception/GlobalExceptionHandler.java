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

    // 405: 메서드 안 맞음
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<APIResponse<?>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e,
                                                                 HttpServletRequest request) {
        log.warn("[405] {} {} - {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(APIResponse.error(HttpStatus.METHOD_NOT_ALLOWED));
    }

    // 415: Content-Type 문제
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<APIResponse<?>> handleUnsupportedMedia(HttpMediaTypeNotSupportedException e,
                                                                 HttpServletRequest request) {
        log.warn("[415] {} {} - {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(APIResponse.error(HttpStatus.UNSUPPORTED_MEDIA_TYPE));
    }

    // 409: 유니크 제약/무결성 위반 등 (DB 충돌)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<APIResponse<?>> handleConflict(DataIntegrityViolationException e,
                                                         HttpServletRequest request) {
        log.warn("[409] {} {} - {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(APIResponse.error(HttpStatus.CONFLICT));
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