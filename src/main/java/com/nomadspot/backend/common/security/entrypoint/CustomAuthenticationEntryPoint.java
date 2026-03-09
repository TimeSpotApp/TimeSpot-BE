package com.nomadspot.backend.common.security.entrypoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomadspot.backend.common.response.BaseResponse;
import com.nomadspot.backend.common.response.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * PackageName : com.nomadspot.backend.common.security.entrypoint
 * FileName    : CustomAuthenticationEntryPoint
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
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        log.error("인증 실패: {}", authException.getMessage());

        ErrorCode errorCode = (ErrorCode) request.getAttribute("exception");

        if (errorCode == null) errorCode = ErrorCode.UNAUTHORIZED;

        BaseResponse<Void> baseResponse = BaseResponse.error(errorCode);

        response.setStatus(baseResponse.getCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getWriter(), baseResponse);
    }

}
