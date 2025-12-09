package org.restapi.springrestapi.controller.support;

import com.google.gson.Gson;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.restapi.springrestapi.security.jwt.JwtFilter;
import org.restapi.springrestapi.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.BDDMockito.given;

/**
 * 공통 WebMvcTest 지원 클래스.
 * JwtFilter/JwtProvider를 MockBean으로 주입해 컨트롤러 테스트마다 반복 선언하지 않도록 한다.
 */
public abstract class ControllerTestSupport {

    protected static final Gson GSON = new Gson();

    @Autowired protected MockMvc mockMvc;

    @MockitoBean protected JwtProvider jwtProvider;
    @MockitoBean protected JwtFilter jwtFilter;

    @BeforeEach
    void setUpSecurityMocks() throws Exception {
        reset(jwtProvider, jwtFilter);
        given(jwtProvider.resolveAccessToken(any(HttpServletRequest.class))).willReturn(Optional.empty());
        given(jwtProvider.resolveRefreshToken(any(HttpServletRequest.class))).willReturn(Optional.empty());
        doAnswer(invocation -> {
            HttpServletRequest req = invocation.getArgument(0);
            HttpServletResponse res = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());
    }
}
