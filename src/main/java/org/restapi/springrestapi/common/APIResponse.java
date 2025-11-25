package org.restapi.springrestapi.common;

import java.util.Map;

import org.restapi.springrestapi.exception.code.ErrorCode;
import org.restapi.springrestapi.exception.code.SuccessCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@Builder
public class APIResponse<T> {
	private String message;
	private T Data;

	public static <T> APIResponse<T> ok(SuccessCode code, T data) {
		return new APIResponse<>(code.getMessage(), data);
	}

    public static APIResponse<?> error(ErrorCode code) {
        return new APIResponse<>(code.getMessage(), Map.of());
    }
    public static APIResponse<?> error(String message) {
        return new APIResponse<>(message, Map.of());
    }
    public static APIResponse<?> error(HttpStatus status) {
        return new APIResponse<>(status.getReasonPhrase(), Map.of());
    }
}
