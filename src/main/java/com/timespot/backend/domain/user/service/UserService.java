package com.timespot.backend.domain.user.service;

import com.timespot.backend.domain.user.dto.UserRequestDto;
import com.timespot.backend.domain.user.dto.UserResponseDto.UserInfoResponse;
import com.timespot.backend.domain.user.model.ProviderType;
import com.timespot.backend.domain.user.model.User;
import java.util.Optional;
import java.util.UUID;

/**
 * PackageName : com.timespot.backend.domain.user.service
 * FileName    : UserService
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
public interface UserService {

    Optional<User> findUserForSocialConnection(ProviderType providerType, String providerUserId);

    User createUserForSocialConnection(ProviderType providerType,
                                       String providerUserId,
                                       String email,
                                       String nickname,
                                       String authorizationCode);

    User findById(UUID id);

    UserInfoResponse findUserInfoById(UUID id);

    void updateUserInfo(UUID id, UserRequestDto.UserInfoUpdateRequest dto);

    void withdraw(UUID id);

}
