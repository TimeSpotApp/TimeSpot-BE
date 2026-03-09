package com.nomadspot.backend.common.security.jwt.filter;

import com.nomadspot.backend.common.security.constant.SecurityConst;
import com.nomadspot.backend.common.security.jwt.provider.JwtProvider;
import com.nomadspot.backend.infra.redis.constant.RedisConst;
import com.nomadspot.backend.infra.redis.dao.RedisRepository;
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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * PackageName : com.nomadspot.backend.common.security.jwt.filter
 * FileName    : JwtAuthenticationFilter
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
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider     jwtProvider;
    private final RedisRepository redisRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        final String token = resolveToken(request);

        if (token != null
            && !redisRepository.hasKey("%s%s".formatted(RedisConst.JWT_ACCESS_TOKEN_BLACKLIST_PREFIX, token))
            && jwtProvider.validateToken(token, false)) {
            Authentication authentication = jwtProvider.getAuthenticationFromToken(token, false);

            if (authentication instanceof UsernamePasswordAuthenticationToken userPasswordAuthenticationToken)
                userPasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

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
        String bearerToken = request.getHeader(SecurityConst.JWT_ACCESS_TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(SecurityConst.JWT_ACCESS_TOKEN_PREFIX))
            return bearerToken.substring(7);
        return null;
    }

}
