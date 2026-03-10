package com.nomadspot.backend.common.security.service;

import com.nomadspot.backend.common.security.dto.AuthRequestDto;
import com.nomadspot.backend.common.security.dto.AuthResponseDto;

/**
 * PackageName : com.nomadspot.backend.common.security.service
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

    AuthResponseDto.TokenResponse login(String provider, AuthRequestDto.OAuth2LoginRequest dto);

    void logout(String accessToken);

    AuthResponseDto.TokenResponse refresh(String refreshToken);

}
