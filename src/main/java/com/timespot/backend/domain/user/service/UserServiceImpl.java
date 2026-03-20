package com.timespot.backend.domain.user.service;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.common.response.ErrorCode;
import com.timespot.backend.domain.user.dao.SocialConnectionRepository;
import com.timespot.backend.domain.user.dao.UserRepository;
import com.timespot.backend.domain.user.dto.UserRequestDto;
import com.timespot.backend.domain.user.dto.UserResponseDto.UserInfoResponse;
import com.timespot.backend.domain.user.model.ProviderType;
import com.timespot.backend.domain.user.model.SocialConnection;
import com.timespot.backend.domain.user.model.User;
import com.timespot.backend.infra.security.oauth.client.IdpTokenExchangeClient;
import com.timespot.backend.infra.security.oauth.constant.TokenType;
import com.timespot.backend.infra.security.oauth.dto.OAuthResponseDto.AppleTokenValidationResponse;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PackageName : com.timespot.backend.domain.user.service
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

    private final IdpTokenExchangeClient idpTokenExchangeClient;

    /**
     * 소셜 인증 정보를 사용하여 회원 정보 조회
     *
     * @param providerType   소셜 인증 제공자 유형
     * @param providerUserId 소셜 인증 제공자 식별자
     * @return 회원 엔티티(Optional)
     */
    @Override
    public Optional<User> findUserForSocialConnection(final ProviderType providerType, final String providerUserId) {
        Optional<SocialConnection> opSocialConnection = socialConnectionRepository.findByProviderTypeAndProviderId(
                providerType, providerUserId
        );
        return opSocialConnection.map(SocialConnection::getUser);
    }

    /**
     * 소셜 인증 정보를 사용하여 회원 정보 생성
     *
     * @param providerType      소셜 인증 제공자 유형
     * @param providerUserId    소셜 인증 제공자 식별자
     * @param email             이메일
     * @param nickname          닉네임
     * @param authorizationCode 소셜 인증 코드
     * @return 회원 엔티티
     */
    @Override
    @Transactional
    public User createUserForSocialConnection(final ProviderType providerType,
                                              final String providerUserId,
                                              final String email,
                                              final String nickname,    // BODGE: APPLE 연동 시 닉네임 처리 확인 필요
                                              final String authorizationCode) {
        if (email != null && !email.isBlank())
            if (userRepository.existsByEmail(email))
                throw new GlobalException(ErrorCode.USER_EMAIL_DUPLICATED);

        final String idpRefreshToken;
        switch (providerType) {
            case APPLE -> {
                AppleTokenValidationResponse tokenValidationResponse = idpTokenExchangeClient.validationAppleAuthCode(
                        authorizationCode
                );
                idpRefreshToken = tokenValidationResponse.refreshToken();

                User user = userRepository.save(User.of(email, nickname));
                socialConnectionRepository.save(
                        SocialConnection.of(user, providerType, providerUserId, idpRefreshToken)
                );

                return user;
            }
            case GOOGLE -> {
                //GoogleTokenValidationResponse tokenValidationResponse = idpTokenExchangeClient.validationGoogleAuthCode(
                //        authorizationCode
                //);
                //idpRefreshToken = tokenValidationResponse.refreshToken();
                idpRefreshToken = null; // NOTE: Google 계정의 경우 연동 인증 및 IDP 토큰 발급 처리 스킵

                User user = userRepository.save(User.of(email, nickname));
                socialConnectionRepository.save(SocialConnection.of(user, providerType, providerUserId));

                return user;
            }
            default -> throw new GlobalException(ErrorCode.SOCIAL_CONNECTION_PROVIDER_NOT_SUPPORTED);
        }
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
     * 회원 정보 수정
     *
     * @param id  회원 ID
     * @param dto 회원 정보 수정 요청 DTO
     */
    @Override
    @Transactional
    public void updateUserInfo(final UUID id, final UserRequestDto.UserInfoUpdateRequest dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
        user.updateNickname(dto.getNewNickname());
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

        switch (socialConnection.getProviderType()) {
            case APPLE -> idpTokenExchangeClient.revokeAppleToken(TokenType.REFRESH_TOKEN,
                                                                  socialConnection.getIdpRefreshToken());
            case GOOGLE -> {}
            // NOTE: Google 계정의 경우 연동 해지 시 IDP 토큰 폐기 스킵
            //idpTokenExchangeClient.revokeGoogleToken(socialConnection.getIdpRefreshToken());
            default -> throw new GlobalException(ErrorCode.SOCIAL_CONNECTION_PROVIDER_NOT_SUPPORTED);
        }

        socialConnectionRepository.delete(socialConnection);
        userRepository.delete(user);
    }

}
