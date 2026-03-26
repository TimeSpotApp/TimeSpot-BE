package com.timespot.backend.common.security.service;

import com.timespot.backend.common.security.dto.AuthRequestDto.OAuth2LoginRequest;
import com.timespot.backend.common.security.dto.AuthRequestDto.OAuth2SignupRequest;
import com.timespot.backend.common.security.dto.AuthResponseDto.AuthInfoResponse;
import com.timespot.backend.common.security.dto.AuthResponseDto.TokenInfoResponse;
import java.util.UUID;

/**
 * PackageName : com.timespot.backend.common.security.service
 * FileName    : AuthService
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description : 인증 서비스 인터페이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 * 26. 3. 24.    loadingKKamo21       JavaDoc 개선
 */
public interface AuthService {

    /**
     * OAuth2 소셜 회원가입
     *
     * @param dto 소셜 회원가입 요청 DTO
     * @return 인증 정보 응답 DTO
     */
    AuthInfoResponse signup(OAuth2SignupRequest dto);

    /**
     * OAuth2 소셜 로그인
     *
     * @param dto 소셜 로그인 요청 DTO
     * @return 인증 정보 응답 DTO (신규 사용자는 토큰 없음)
     */
    AuthInfoResponse login(OAuth2LoginRequest dto);

    /**
     * 로그아웃 (AccessToken 블랙리스트 처리)
     *
     * @param accessToken AccessToken
     */
    void logout(String accessToken);

    /**
     * RefreshToken 으로 AccessToken 갱신
     *
     * @param refreshToken RefreshToken
     * @return 갱신된 토큰 정보 응답 DTO
     * @throws com.timespot.backend.common.error.GlobalException (USER_AUTH_INVALID_REFRESH_TOKEN, USER_AUTH_REFRESH_TOKEN_EXPIRED)
     */
    TokenInfoResponse refresh(String refreshToken);

    /**
     * 회원 ID 기반 토큰 재발급 (회원 정보 수정 시)
     *
     * @param userId 회원 ID
     * @return 재발급된 인증 정보 응답 DTO
     */
    AuthInfoResponse reissueTokenByUserId(UUID userId);

}
