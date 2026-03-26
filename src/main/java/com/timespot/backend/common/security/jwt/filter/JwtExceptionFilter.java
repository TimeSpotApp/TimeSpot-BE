package com.timespot.backend.common.security.jwt.filter;

import static com.timespot.backend.common.response.ErrorCode.USER_AUTH_ACCESS_TOKEN_EXPIRED;
import static com.timespot.backend.common.response.ErrorCode.USER_AUTH_INVALID_ACCESS_TOKEN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * PackageName : com.timespot.backend.common.security.jwt.filter
 * FileName    : JwtExceptionFilter
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description : JWT 예외 처리 및 에러 응답 변환 필터
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@RequiredArgsConstructor
@Slf4j
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명", e);
            sendErrorResponse(response, USER_AUTH_INVALID_ACCESS_TOKEN);
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT", e);
            sendErrorResponse(response, USER_AUTH_ACCESS_TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 JWT 형식", e);
            sendErrorResponse(response, USER_AUTH_INVALID_ACCESS_TOKEN);
        } catch (JwtException e) {
            log.error("알 수 없는 JWT 에러", e);
            sendErrorResponse(response, USER_AUTH_INVALID_ACCESS_TOKEN);
        }
    }

    // ========================= 내부 메서드 =========================

    /**
     * 에러 응답 생성
     *
     * @param response  HttpServletResponse
     * @param errorCode 에러 코드
     * @throws IOException IOException
     */
    private void sendErrorResponse(final HttpServletResponse response, final ErrorCode errorCode) throws IOException {
        BaseResponse<Void> baseResponse = BaseResponse.error(errorCode);

        response.setStatus(baseResponse.getCode());
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(UTF_8.name());

        objectMapper.writeValue(response.getWriter(), baseResponse);
    }

}
