package com.timespot.backend.common.security.service;

import com.timespot.backend.common.security.dto.AuthRequestDto;
import com.timespot.backend.common.security.dto.AuthResponseDto;
import java.util.UUID;

/**
 * PackageName : com.timespot.backend.common.security.service
 * FileName    : AuthService
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
public interface AuthService {

    AuthResponseDto.AuthInfoResponse signup(AuthRequestDto.OAuth2SignupRequest dto);

    AuthResponseDto.AuthInfoResponse login(AuthRequestDto.OAuth2LoginRequest dto);

    void logout(String accessToken);

    AuthResponseDto.AuthInfoResponse refresh(String refreshToken);

    AuthResponseDto.AuthInfoResponse reissueTokenByUserId(UUID userId);

}
