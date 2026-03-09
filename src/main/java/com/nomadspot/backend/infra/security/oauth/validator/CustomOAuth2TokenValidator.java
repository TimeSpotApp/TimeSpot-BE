package com.nomadspot.backend.infra.security.oauth.validator;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.nomadspot.backend.common.error.GlobalException;
import com.nomadspot.backend.common.response.ErrorCode;
import com.nomadspot.backend.domain.user.model.ProviderType;
import com.nomadspot.backend.infra.security.oauth.constant.OAuthConst;
import com.nomadspot.backend.infra.security.oauth.properties.OAuth2Properties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Locator;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * PackageName : com.nomadspot.backend.infra.security.oauth.validator
 * FileName    : CustomOAuth2TokenValidator
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@Component
@Slf4j
public class CustomOAuth2TokenValidator {

    private final JwkProvider appleJwkProvider;
    private final JwkProvider googleJwkProvider;

    private final String appleClientId;
    private final String googleClientId;

    public CustomOAuth2TokenValidator(final OAuth2Properties oAuth2Properties) throws MalformedURLException {
        appleJwkProvider = new JwkProviderBuilder(new URL(OAuthConst.APPLE_JWKS_URL))
                .cached(10, 10, TimeUnit.HOURS).build();
        googleJwkProvider = new JwkProviderBuilder(new URL(OAuthConst.GOOGLE_JWKS_URL))
                .cached(10, 10, TimeUnit.HOURS).build();
        appleClientId = oAuth2Properties.getApple().getClientId();
        googleClientId = oAuth2Properties.getGoogle().getClientId();
    }

    /**
     * 소셜 인증 제공자 ID Token 서명 검증 및 페이로드 추출
     *
     * @param provider 소셜 인증 제공자 유형
     * @param token    인증 토큰
     * @return Payload
     */
    public Claims verifyAndParse(final String provider, final String token) {
        Locator<Key> keyLocator = locator -> {
            JwsHeader header = (JwsHeader) locator;
            String    keyId  = header.getKeyId();

            try {
                if (ProviderType.APPLE.name().equalsIgnoreCase(provider))
                    return appleJwkProvider.get(keyId).getPublicKey();
                else if (ProviderType.GOOGLE.name().equalsIgnoreCase(provider))
                    return googleJwkProvider.get(keyId).getPublicKey();
                throw new GlobalException(ErrorCode.SOCIAL_CONNECTION_PROVIDER_NOT_SUPPORTED);
            } catch (JwkException e) {
                log.error("{} 공개 키 조회 실패 - keyId: {}", provider, keyId, e);
                throw new GlobalException(ErrorCode.SOCIAL_CONNECTION_TOKEN_PARSE_FAILED);
            }
        };

        String expectedIssuer = ProviderType.APPLE.name().equalsIgnoreCase(provider)
                                ? OAuthConst.APPLE_ISSUER
                                : ProviderType.GOOGLE.name().equalsIgnoreCase(provider)
                                  ? OAuthConst.GOOGLE_ISSUER
                                  : null;
        String expectedAudience = ProviderType.APPLE.name().equalsIgnoreCase(provider)
                                  ? appleClientId
                                  : ProviderType.GOOGLE.name().equalsIgnoreCase(provider)
                                    ? googleClientId
                                    : null;

        try {
            return Jwts.parser()
                       .keyLocator(keyLocator)
                       .requireIssuer(expectedIssuer)
                       .requireAudience(expectedAudience)
                       .build()
                       .parseSignedClaims(token)
                       .getPayload();
        } catch (Exception e) {
            log.error("잘못되거나 만료된 {} 토큰입니다.", provider, e);
            throw new GlobalException(ErrorCode.SOCIAL_CONNECTION_INVALID_TOKEN);
        }
    }

}
