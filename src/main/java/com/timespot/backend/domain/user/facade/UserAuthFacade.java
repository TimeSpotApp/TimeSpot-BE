package com.timespot.backend.domain.user.facade;

import com.timespot.backend.common.security.dto.AuthResponseDto;
import com.timespot.backend.common.security.service.AuthService;
import com.timespot.backend.domain.user.dto.UserRequestDto;
import com.timespot.backend.domain.user.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PackageName : com.timespot.backend.domain.user.facade
 * FileName    : UserAuthFacade
 * Author      : loadingKKamo21
 * Date        : 26. 3. 21.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 21.    loadingKKamo21       Initial creation
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserAuthFacade {

    private final UserService userService;
    private final AuthService authService;

    /**
     * 회원 정보 수정 및 인증 토큰 재발급
     *
     * @param id  회원 ID
     * @param dto 회원 정보 수정 요청 DTO
     * @return 재발급된 인증 정보 응답 DTO
     */
    @Transactional
    public AuthResponseDto.AuthInfoResponse updateUserInfoAndReissueToken(
            final UUID id, final UserRequestDto.UserInfoUpdateRequest dto
    ) {
        userService.updateUserInfo(id, dto);
        return authService.reissueTokenByUserId(id);
    }

}
