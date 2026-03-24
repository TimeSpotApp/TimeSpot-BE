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
 */
public interface AuthService {

    AuthInfoResponse signup(OAuth2SignupRequest dto);

    AuthInfoResponse login(OAuth2LoginRequest dto);

    void logout(String accessToken);

    TokenInfoResponse refresh(String refreshToken);

    AuthInfoResponse reissueTokenByUserId(UUID userId);

}
