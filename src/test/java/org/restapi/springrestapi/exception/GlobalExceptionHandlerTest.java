package org.restapi.springrestapi.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.restapi.springrestapi.common.APIResponse;
import org.restapi.springrestapi.exception.code.AuthErrorCode;
import org.restapi.springrestapi.exception.code.CommonErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

class GlobalExceptionHandlerTest {

    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("MethodArgumentNotValidException은 BAD_REQUEST를 반환한다")
    void handleMethodArgumentNotValidException_returnsBadRequest() throws Exception {
        BindingResult bindingResult = new BeanPropertyBindingResult(new DummyRequest(""), "dummyRequest");
        bindingResult.addError(new FieldError("dummyRequest", "name", "필수값입니다"));
        MethodParameter methodParameter = new MethodParameter(DummyController.class.getDeclaredMethod("endpoint", DummyRequest.class), 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<APIResponse<?>> response = handler.handleMethodArgumentNotValidException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("필수값입니다");
    }

    @Test
    @DisplayName("AppException은 정의된 코드와 상태를 반환한다")
    void handleAppException_returnsCustomStatus() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        AppException exception = new AppException(AuthErrorCode.UNAUTHORIZED);

        ResponseEntity<APIResponse<?>> response = handler.handleAppException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(AuthErrorCode.UNAUTHORIZED.getStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(AuthErrorCode.UNAUTHORIZED.getMessage());
    }

    @Test
    @DisplayName("기타 예외는 내부 서버 오류를 반환한다")
    void handleAnyException_returnsInternalServerError() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Exception exception = new IllegalStateException("boom");

        ResponseEntity<APIResponse<?>> response = handler.handleAnyException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(CommonErrorCode.INTERNAL.getCode());
    }

    static class DummyController {
        void endpoint(@Valid DummyRequest request) {}
    }

    record DummyRequest(@NotBlank String name) {}
}
