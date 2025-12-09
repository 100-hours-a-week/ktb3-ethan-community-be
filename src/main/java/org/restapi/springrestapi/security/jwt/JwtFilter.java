package org.restapi.springrestapi.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.AuthException;
import org.restapi.springrestapi.exception.code.AuthErrorCode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final AuthenticationEntryPoint entryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            // CORS preflight 통과
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                filterChain.doFilter(request, response);
                return;
            }

            // refresh 엔드포인트 통과
            if (JwtProvider.REFRESH_PATH.equals(request.getRequestURI())) {
                Optional<String> refreshTokenOpt;
                try {
                    refreshTokenOpt = jwtProvider.resolveRefreshToken(request);
                } catch (AppException ex) {
                    throw new AuthException(ex.getErrorCode());
                }
                if (refreshTokenOpt.isEmpty()) {
                    throw new AuthException(AuthErrorCode.REFRESH_COOKIE_MISSING);
                }
                String refreshToken = refreshTokenOpt.get();

                if (!jwtProvider.validateRefreshToken(refreshToken)) {
                    // refresh가 있는데 invalid → 재로그인 필요
                    throw new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID);
                }
                // 이후 서비스 계층에서는 그냥 리프레쉬 가져다 쓰면 됨
                filterChain.doFilter(request, response);
                return;
            }

            Optional<String> accessTokenOpt = jwtProvider.resolveAccessToken(request);

            if (accessTokenOpt.isPresent()) {
                String accessToken = accessTokenOpt.get();
                if (jwtProvider.validateAccessToken(accessToken)) {
                    SecurityContextHolder.getContext().setAuthentication(jwtProvider.getAuthentication(accessToken));
                } else {
                /*
                엑세스 토큰이 존재 = 로그인한 사용자
                그러나 토큰이 유효하지 않음 = 위조 되었거나 시간 만료
                로그인 사용자에게 올바른 정보를 보여줄 수 없으니 예외를 발생시켜 리프레쉬를 유도해야 한다.
                 */
                    throw new AuthException(AuthErrorCode.ACCESS_TOKEN_EXPIRED);
                }
            }

            filterChain.doFilter(request, response);
        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            entryPoint.commence(request, response, ex);
        }
    }
}
