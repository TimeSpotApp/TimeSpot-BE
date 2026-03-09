package com.nomadspot.backend.domain.user.service;

import com.nomadspot.backend.common.error.GlobalException;
import com.nomadspot.backend.common.response.ErrorCode;
import com.nomadspot.backend.domain.user.dao.SocialConnectionRepository;
import com.nomadspot.backend.domain.user.dao.UserRepository;
import com.nomadspot.backend.domain.user.dto.UserResponseDto.UserInfoResponse;
import com.nomadspot.backend.domain.user.model.ProviderType;
import com.nomadspot.backend.domain.user.model.SocialConnection;
import com.nomadspot.backend.domain.user.model.User;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PackageName : com.nomadspot.backend.domain.user.service
 * FileName    : UserServiceImpl
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository             userRepository;
    private final SocialConnectionRepository socialConnectionRepository;

    /**
     * 소셜 인증 제공자로부터 회원 정보를 저장하거나, 이미 존재하는 경우 기존 회원 정보를 반환
     *
     * @return 회원 엔티티
     */
    @Override
    @Transactional
    public User findOrCreateUserForSocialConnection(final ProviderType providerType,
                                                    final String providerUserId,
                                                    final String email,
                                                    final String nickname) {
        Optional<SocialConnection> opSocialConnection = socialConnectionRepository.findByProviderTypeAndProviderId(
                providerType, providerUserId
        );
        if (opSocialConnection.isPresent()) return opSocialConnection.get().getUser();

        if (email != null && !email.isBlank())
            if (userRepository.existsByEmail(email))
                throw new GlobalException(ErrorCode.USER_EMAIL_DUPLICATED);

        User user = userRepository.save(User.of(email, nickname));
        socialConnectionRepository.save(SocialConnection.of(user, providerType, providerUserId));

        return user;
    }

    /**
     * ID로 회원 정보 조회
     *
     * @param id 회원 ID
     * @return 회원 엔티티
     */
    @Override
    public User findById(final UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * ID로 회원 정보 조회
     *
     * @param id 회원 ID
     * @return 회원 정보 응답 DTO
     */
    @Override
    public UserInfoResponse findUserInfoById(final UUID id) {
        return userRepository.findUserInfoById(id).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * ID로 회원 탈퇴
     *
     * @param id 회원 ID
     */
    @Override
    @Transactional
    public void withdraw(final UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
        SocialConnection socialConnection = socialConnectionRepository.findByUserId(id)
                                                                      .orElseThrow(() -> new GlobalException(
                                                                              ErrorCode.SOCIAL_CONNECTION_NOT_FOUND
                                                                      ));
        socialConnectionRepository.delete(socialConnection);
        userRepository.delete(user);
        // TODO: 탈퇴한 회원 정보 보관 정책 및 엔티티/DB 구조 결정 시 정보 보관 로직 추가
    }

}
