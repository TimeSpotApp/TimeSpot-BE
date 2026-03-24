package com.timespot.backend.infra.security.oauth.client;

import static com.timespot.backend.common.response.ErrorCode.SOCIAL_CONNECTION_IDP_TOKEN_REFRESH_FAILED;
import static com.timespot.backend.common.response.ErrorCode.SOCIAL_CONNECTION_IDP_TOKEN_REVOKE_FAILED;
import static com.timespot.backend.common.response.ErrorCode.SOCIAL_CONNECTION_IDP_TOKEN_VALIDATION_FAILED;
import static com.timespot.backend.infra.security.oauth.constant.OAuthConst.APPLE_IDP_TOKEN_REVOKE_URL;
import static com.timespot.backend.infra.security.oauth.constant.OAuthConst.APPLE_IDP_TOKEN_URL;
import static com.timespot.backend.infra.security.oauth.constant.OAuthConst.APPLE_ISSUER;
import static com.timespot.backend.infra.security.oauth.constant.OAuthConst.GOOGLE_IDP_TOKEN_REVOKE_URL;
import static com.timespot.backend.infra.security.oauth.constant.OAuthConst.GOOGLE_IDP_TOKEN_URL;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.common.response.ErrorCode;
import com.timespot.backend.infra.security.oauth.constant.TokenType;
import com.timespot.backend.infra.security.oauth.dto.OAuthRequestFactory;
import com.timespot.backend.infra.security.oauth.dto.OAuthResponseDto.AppleTokenRefreshResponse;
import com.timespot.backend.infra.security.oauth.dto.OAuthResponseDto.AppleTokenValidationResponse;
import com.timespot.backend.infra.security.oauth.dto.OAuthResponseDto.GoogleTokenRefreshResponse;
import com.timespot.backend.infra.security.oauth.dto.OAuthResponseDto.GoogleTokenValidationResponse;
import com.timespot.backend.infra.security.oauth.properties.OAuth2Properties;
import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * PackageName : com.timespot.backend.infra.security.oauth.client
 * FileName    : IdpTokenExchangeClient
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description : IDP (Apple/Google) 토큰 교환 클라이언트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 * 26. 3. 24.    loadingKKamo21       중복 로직 제거 및 최적화 (공통 메서드 추출)
 */
@Component
@Slf4j
public class IdpTokenExchangeClient {

    private final RestClient restClient;

    private final String appleClientId;
    private final String appleTeamId;
    private final String appleKeyId;
    private final String applePrivateKey;
    private final String appleRedirectUri;

    private final String googleClientId;
    private final String googleClientSecret;
    private final String googleRedirectUri;

    public IdpTokenExchangeClient(final RestClient.Builder builder, final OAuth2Properties oAuth2Properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(5).toMillis());

        restClient = builder.requestFactory(factory).build();

        appleClientId = oAuth2Properties.getApple().getClientId();
        appleTeamId = oAuth2Properties.getApple().getTeamId();
        appleKeyId = oAuth2Properties.getApple().getKeyId();
        applePrivateKey = oAuth2Properties.getApple().getPrivateKey();
        appleRedirectUri = oAuth2Properties.getApple().getRedirectUri();

        googleClientId = oAuth2Properties.getGoogle().getClientId();
        googleClientSecret = oAuth2Properties.getGoogle().getClientSecret();
        googleRedirectUri = oAuth2Properties.getGoogle().getRedirectUri();
    }

    /**
     * Apple 인증 코드 검증 및 토큰 교환
     */
    public AppleTokenValidationResponse validationAppleAuthCode(final String authCode) {
        MultiValueMap<String, String> params = OAuthRequestFactory.createAppleTokenValidationRequest(
                appleClientId, createAppleClientSecret(), authCode, appleRedirectUri
        );
        return executeAppleTokenValidation(params);
    }

    /**
     * Google 인증 코드 검증 및 토큰 교환
     */
    public GoogleTokenValidationResponse validationGoogleAuthCode(final String authCode) {
        MultiValueMap<String, String> params = OAuthRequestFactory.createGoogleTokenValidationRequest(
                googleClientId, googleClientSecret, authCode, googleRedirectUri
        );
        return executeGoogleTokenValidation(params);
    }

    /**
     * Apple AccessToken 갱신
     */
    public AppleTokenRefreshResponse refreshAppleAccessToken(final String idpRefreshToken) {
        MultiValueMap<String, String> params = OAuthRequestFactory.createAppleTokenRefreshRequest(
                appleClientId, createAppleClientSecret(), idpRefreshToken
        );
        return executeAppleTokenRefresh(params);
    }

    /**
     * Google AccessToken 갱신
     */
    public GoogleTokenRefreshResponse refreshGoogleAccessToken(final String idpRefreshToken) {
        MultiValueMap<String, String> params = OAuthRequestFactory.createGoogleTokenRefreshRequest(
                googleClientId, googleClientSecret, idpRefreshToken
        );
        return executeGoogleTokenRefresh(params);
    }

    /**
     * Apple 계정 연동 해제 (토큰 폐기)
     */
    public void revokeAppleToken(final TokenType tokenType, final String token) {
        MultiValueMap<String, String> params = OAuthRequestFactory.createAppleTokenRevokeRequest(
                appleClientId, createAppleClientSecret(), tokenType, token
        );
        executeTokenRevoke(APPLE_IDP_TOKEN_REVOKE_URL, params, "Apple");
    }

    /**
     * Google 계정 연동 해제 (토큰 폐기)
     */
    public void revokeGoogleToken(final String token) {
        ResponseEntity<String> response = restClient.post()
                                                    .uri(GOOGLE_IDP_TOKEN_REVOKE_URL + "?token={token}", token)
                                                    .contentType(APPLICATION_FORM_URLENCODED)
                                                    .retrieve()
                                                    .toEntity(String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Google 계정 토큰 폐기 실패: status={}, body={}", response.getStatusCode(), response.getBody());
            throw new GlobalException(SOCIAL_CONNECTION_IDP_TOKEN_REVOKE_FAILED,
                                      "Google revoke failed: " + response.getBody());
        }
    }

    // ========================= 내부 메서드 =========================

    /**
     * Apple Client Secret 생성 (JWT)
     */
    private String createAppleClientSecret() {
        try {
            String privateKeyPEM = applePrivateKey.replace("-----BEGIN PRIVATE KEY-----", "")
                                                  .replace("-----END PRIVATE KEY-----", "")
                                                  .replaceAll("\\s", "");

            byte[]     encoded    = Base64.getDecoder().decode(privateKeyPEM);
            PrivateKey privateKey = KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(encoded));

            return Jwts.builder()
                       .header().keyId(appleKeyId).and()
                       .issuer(appleTeamId)
                       .audience().add(APPLE_ISSUER).and()
                       .subject(appleClientId)
                       .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 5))
                       .signWith(privateKey)
                       .compact();
        } catch (Exception e) {
            log.error("Apple 클라이언트 시크릿 생성 실패: {}", e.getMessage());
            throw new GlobalException(SOCIAL_CONNECTION_IDP_TOKEN_VALIDATION_FAILED);
        }
    }

    /**
     * 공통 토큰 폐기 로직
     */
    private void executeTokenRevoke(final String revokeUrl,
                                    final MultiValueMap<String, String> params,
                                    final String provider) {
        try {
            ResponseEntity<String> response = restClient.post()
                                                        .uri(revokeUrl)
                                                        .contentType(APPLICATION_FORM_URLENCODED)
                                                        .body(params)
                                                        .retrieve()
                                                        .toEntity(String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("{} 계정 토큰 폐기 실패: status={}, body={}", provider, response.getStatusCode(), response.getBody());
                throw new GlobalException(SOCIAL_CONNECTION_IDP_TOKEN_REVOKE_FAILED,
                                          provider + " revoke failed: " + response.getBody());
            }
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            log.error("{} 계정 연동 해지 실패: {}", provider, e.getMessage());
            throw new GlobalException(SOCIAL_CONNECTION_IDP_TOKEN_REVOKE_FAILED, e);
        }
    }

    /**
     * Apple 토큰 검증 실행
     */
    private AppleTokenValidationResponse executeAppleTokenValidation(
            final MultiValueMap<String, String> params) {
        return executeWithFallback(
                () -> restClient.post()
                                .uri(APPLE_IDP_TOKEN_URL)
                                .contentType(APPLICATION_FORM_URLENCODED)
                                .body(params)
                                .retrieve()
                                .body(AppleTokenValidationResponse.class),
                APPLE_IDP_TOKEN_URL,
                "Apple 계정 인증",
                SOCIAL_CONNECTION_IDP_TOKEN_VALIDATION_FAILED
        );
    }

    /**
     * Google 토큰 검증 실행
     */
    private GoogleTokenValidationResponse executeGoogleTokenValidation(
            final MultiValueMap<String, String> params) {
        return executeWithFallback(
                () -> restClient.post()
                                .uri(GOOGLE_IDP_TOKEN_URL)
                                .contentType(APPLICATION_FORM_URLENCODED)
                                .body(params)
                                .retrieve()
                                .body(GoogleTokenValidationResponse.class),
                GOOGLE_IDP_TOKEN_URL,
                "Google 계정 인증",
                SOCIAL_CONNECTION_IDP_TOKEN_VALIDATION_FAILED
        );
    }

    /**
     * Apple 토큰 갱신 실행
     */
    private AppleTokenRefreshResponse executeAppleTokenRefresh(
            final MultiValueMap<String, String> params) {
        return executeWithFallback(
                () -> restClient.post()
                                .uri(APPLE_IDP_TOKEN_URL)
                                .contentType(APPLICATION_FORM_URLENCODED)
                                .body(params)
                                .retrieve()
                                .body(AppleTokenRefreshResponse.class),
                APPLE_IDP_TOKEN_URL,
                "Apple 계정 토큰 갱신",
                SOCIAL_CONNECTION_IDP_TOKEN_REFRESH_FAILED
        );
    }

    /**
     * Google 토큰 갱신 실행
     */
    private GoogleTokenRefreshResponse executeGoogleTokenRefresh(
            final MultiValueMap<String, String> params) {
        return executeWithFallback(
                () -> restClient.post()
                                .uri(GOOGLE_IDP_TOKEN_URL)
                                .contentType(APPLICATION_FORM_URLENCODED)
                                .body(params)
                                .retrieve()
                                .body(GoogleTokenRefreshResponse.class),
                GOOGLE_IDP_TOKEN_URL,
                "Google 계정 토큰 갱신",
                SOCIAL_CONNECTION_IDP_TOKEN_REFRESH_FAILED
        );
    }

    /**
     * 공통 예외 처리 래퍼
     */
    private <T> T executeWithFallback(final SupplierThrowing<T> supplier,
                                      final String url,
                                      final String action,
                                      final ErrorCode errorCode) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("{} 실패 URL: {}, 원인: {}", action, url, e.getMessage());
            throw new GlobalException(errorCode, action + " 실패\n" + errorCode.getMessage());
        }
    }

    /**
     * 예외를 던질 수 있는 Supplier 함수형 인터페이스
     */
    @FunctionalInterface
    private interface SupplierThrowing<T> {
        T get() throws Exception;
    }

}
