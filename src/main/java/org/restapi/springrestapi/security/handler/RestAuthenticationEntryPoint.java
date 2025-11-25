package org.restapi.springrestapi.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.restapi.springrestapi.common.APIResponse;
import org.restapi.springrestapi.exception.AuthException;
import org.restapi.springrestapi.exception.code.AuthErrorCode;
import org.restapi.springrestapi.exception.code.ErrorCode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        ErrorCode code = AuthErrorCode.UNAUTHORIZED;

        if (authException instanceof AuthException ae) {
            code = ae.getErrorCode();
        }

        log.warn("[Filter 401] {} {} - {}", request.getMethod(), request.getRequestURI(), code.getMessage());

        APIResponse<?> body = APIResponse.error(code);

        response.setStatus(code.getStatus().value());
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}