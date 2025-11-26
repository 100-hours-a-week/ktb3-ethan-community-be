package org.restapi.springrestapi.exception.code;


import org.springframework.http.HttpStatus;

public interface ErrorCode {
	HttpStatus getStatus();
	String getMessage();
    String getCode();
}
