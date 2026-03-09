package com.nomadspot.backend.infra.security.oauth.service;

import com.nomadspot.backend.common.security.model.CustomUserDetails;
import com.nomadspot.backend.domain.user.model.User;
import com.nomadspot.backend.domain.user.service.UserService;
import com.nomadspot.backend.infra.security.oauth.model.OAuthProfile;
import com.nomadspot.backend.infra.security.oauth.model.OAuthProfileFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * PackageName : com.nomadspot.backend.infra.security.oauth.service
 * FileName    : CustomOAuth2UserService
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuthProfile oAuthProfile = OAuthProfileFactory.getOAuthProfile(registrationId, oAuth2User.getAttributes());

        User user = userService.findOrCreateUserForSocialConnection(oAuthProfile.getProvider(),
                                                                    oAuthProfile.getProviderUserId(),
                                                                    oAuthProfile.getEmail(),
                                                                    oAuthProfile.getNickname());

        return CustomUserDetails.of(user.getId(),
                                    user.getEmail(),
                                    user.getRole(),
                                    oAuth2User.getAttributes());
    }

}
