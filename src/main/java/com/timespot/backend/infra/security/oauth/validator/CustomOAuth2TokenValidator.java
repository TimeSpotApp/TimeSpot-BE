package com.timespot.backend.infra.security.oauth.validator;

import static com.timespot.backend.common.response.ErrorCode.SOCIAL_CONNECTION_INVALID_TOKEN;
import static com.timespot.backend.common.response.ErrorCode.SOCIAL_CONNECTION_TOKEN_PARSE_FAILED;
import static com.timespot.backend.infra.security.oauth.constant.OAuthConst.APPLE_ISSUER;
import static com.timespot.backend.infra.security.oauth.constant.OAuthConst.APPLE_JWKS_URL;
import static com.timespot.backend.infra.security.oauth.constant.OAuthConst.GOOGLE_ISSUER;
import static com.timespot.backend.infra.security.oauth.constant.OAuthConst.GOOGLE_JWKS_URL;
import static java.util.concurrent.TimeUnit.HOURS;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.domain.user.model.ProviderType;
import com.timespot.backend.infra.security.oauth.properties.OAuth2Properties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Locator;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * PackageName : com.timespot.backend.infra.security.oauth.validator
 * FileName    : CustomOAuth2TokenValidator
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description : OAuth2 ID Token 검증 및 파싱 컴포넌트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 * 26. 3. 24.    loadingKKamo21       코드 최적화 (중복 제거, 전략 패턴)
 */
@Component
@Slf4j
public class CustomOAuth2TokenValidator {

    private final JwkProvider appleJwkProvider;
    private final JwkProvider googleJwkProvider;

    private final String appleClientId;
    private final String googleClientId;

    public CustomOAuth2TokenValidator(final OAuth2Properties oAuth2Properties) throws MalformedURLException {
        appleJwkProvider = new JwkProviderBuilder(new URL(APPLE_JWKS_URL)).cached(10, 10, HOURS).build();
        googleJwkProvider = new JwkProviderBuilder(new URL(GOOGLE_JWKS_URL)).cached(10, 10, HOURS).build();
        appleClientId = oAuth2Properties.getApple().getClientId();
        googleClientId = oAuth2Properties.getGoogle().getClientId();
    }

    /**
     * 소셜 인증 제공자 ID Token 서명 검증 및 Claims 추출
     *
     * @param provider 소셜 인증 제공자 유형 (APPLE, GOOGLE)
     * @param token    인증 토큰
     * @return JWT Claims
     * @throws GlobalException SOCIAL_CONNECTION_PROVIDER_NOT_SUPPORTED, SOCIAL_CONNECTION_INVALID_TOKEN
     */
    public Claims verifyAndParse(final String provider, final String token) {
        final ProviderType providerType = ProviderType.from(provider);

        OAuth2Config config = getOAuth2Config(providerType);

        Locator<Key> keyLocator = createKeyLocator(provider, config.jwkProvider());

        try {
            return Jwts.parser()
                       .keyLocator(keyLocator)
                       .requireIssuer(config.issuer())
                       .requireAudience(config.audience())
                       .build()
                       .parseSignedClaims(token)
                       .getPayload();
        } catch (Exception e) {
            log.error("잘못되거나 만료된 {} 토큰입니다.", provider, e);
            throw new GlobalException(SOCIAL_CONNECTION_INVALID_TOKEN);
        }
    }

    // ========================= 내부 메서드 =========================

    /**
     * OAuth2 설정 조회 (전략 패턴)
     */
    private OAuth2Config getOAuth2Config(final ProviderType providerType) {
        return switch (providerType) {
            case APPLE -> new OAuth2Config(APPLE_ISSUER, appleClientId, appleJwkProvider);
            case GOOGLE -> new OAuth2Config(GOOGLE_ISSUER, googleClientId, googleJwkProvider);
        };
    }

    /**
     * 공개 키 조회용 KeyLocator 생성
     */
    private Locator<Key> createKeyLocator(final String provider, final JwkProvider jwkProvider) {
        return locator -> {
            JwsHeader header = (JwsHeader) locator;
            String    keyId  = header.getKeyId();

            if (keyId == null) throw new GlobalException(SOCIAL_CONNECTION_TOKEN_PARSE_FAILED);

            try {
                return jwkProvider.get(keyId).getPublicKey();
            } catch (JwkException e) {
                log.error("{} 공개 키 조회 실패 - keyId: {}", provider, keyId, e);
                throw new GlobalException(SOCIAL_CONNECTION_TOKEN_PARSE_FAILED);
            }
        };
    }

    /**
     * OAuth2 설정 레코드
     */
    private record OAuth2Config(String issuer, String audience, JwkProvider jwkProvider) {
    }

}
