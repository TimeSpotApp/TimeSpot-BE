package com.timespot.backend.common.security.jwt.filter;

import static com.timespot.backend.common.security.constant.SecurityConst.JWT_ACCESS_TOKEN_HEADER;
import static com.timespot.backend.common.security.constant.SecurityConst.JWT_ACCESS_TOKEN_PREFIX;
import static com.timespot.backend.infra.redis.constant.RedisConst.JWT_ACCESS_TOKEN_BLACKLIST_PREFIX;

import com.timespot.backend.common.security.jwt.provider.JwtProvider;
import com.timespot.backend.infra.redis.dao.RedisRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * PackageName : com.timespot.backend.common.security.jwt.filter
 * FileName    : JwtAuthenticationFilter
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description : JWT AccessToken 검증 및 SecurityContext 설정 필터
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider     jwtProvider;
    private final RedisRepository redisRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        final String accessToken = resolveToken(request);

        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (redisRepository.hasKey("%s%s".formatted(JWT_ACCESS_TOKEN_BLACKLIST_PREFIX, accessToken)))
            throw new JwtException("블랙리스트에 이미 등록된 토큰입니다.");

        if (jwtProvider.validateAccessToken(accessToken)) {
            Authentication authentication = jwtProvider.getAuthenticationFromAccessToken(accessToken);

            if (authentication instanceof UsernamePasswordAuthenticationToken userPasswordAuthenticationToken)
                userPasswordAuthenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    // ========================= 내부 메서드 =========================

    /**
     * AccessToken 추출
     *
     * @param request HttpServletRequest
     * @return AccessToken 또는 null
     */
    private String resolveToken(final HttpServletRequest request) {
        String bearerToken = request.getHeader(JWT_ACCESS_TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JWT_ACCESS_TOKEN_PREFIX))
            return bearerToken.substring(7);
        return null;
    }

}
