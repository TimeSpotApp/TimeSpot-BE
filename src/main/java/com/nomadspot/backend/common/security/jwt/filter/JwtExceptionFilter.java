package com.nomadspot.backend.common.security.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomadspot.backend.common.response.BaseResponse;
import com.nomadspot.backend.common.response.ErrorCode;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * PackageName : com.nomadspot.backend.common.security.jwt.filter
 * FileName    : JwtExceptionFilter
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@Component
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            BaseResponse<Void> baseResponse = BaseResponse.error(ErrorCode.UNAUTHORIZED, "인증 정보가 유효하지 않거나 만료되었습니다.");

            response.setStatus(baseResponse.getCode());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            objectMapper.writeValue(response.getWriter(), baseResponse);
        } catch (Exception e) {
            throw e;
        }
    }

}
