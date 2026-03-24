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
import com.timespot.backend.infra.security.oauth.dto.OAuthResponseDto;
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
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
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
     * Apple 계정 최초 인증 요청 API 호출
     *
     * @param authCode Apple 인증 코드
     * @return Apple 인증 요청 결과
     */
    public OAuthResponseDto.AppleTokenValidationResponse validationAppleAuthCode(final String authCode) {
        MultiValueMap<String, String> params = OAuthRequestFactory.createAppleTokenValidationRequest(appleClientId,
                                                                                                     createAppleClientSecret(),
                                                                                                     authCode,
                                                                                                     appleRedirectUri);

        return executeAppleTokenValidation(params);
    }

    /**
     * Google 계정 최초 인증 요청 API 호출
     *
     * @param authCode Google 인증 코드
     * @return Google 인증 요청 결과
     */
    public OAuthResponseDto.GoogleTokenValidationResponse validationGoogleAuthCode(final String authCode) {
        MultiValueMap<String, String> params = OAuthRequestFactory.createGoogleTokenValidationRequest(googleClientId,
                                                                                                      googleClientSecret,
                                                                                                      authCode,
                                                                                                      googleRedirectUri);

        return executeGoogleTokenValidation(params);
    }

    /**
     * Apple 계정 IDP 액세스 토큰 갱신
     *
     * @param idpRefreshToken Apple IDP 리프레시 토큰
     * @return Apple 인증 토큰 갱신 결과
     */
    public OAuthResponseDto.AppleTokenRefreshResponse refreshAppleAccessToken(final String idpRefreshToken) {
        MultiValueMap<String, String> params = OAuthRequestFactory.createAppleTokenRefreshRequest(appleClientId,
                                                                                                  createAppleClientSecret(),
                                                                                                  idpRefreshToken);

        return executeAppleTokenRefresh(params);
    }

    /**
     * Google 계정 IDP 액세스 토큰 갱신
     *
     * @param idpRefreshToken Google IDP 리프레시 토큰
     * @return Google 인증 토큰 갱신 결과
     */
    public OAuthResponseDto.GoogleTokenRefreshResponse refreshGoogleAccessToken(final String idpRefreshToken) {
        MultiValueMap<String, String> params = OAuthRequestFactory.createGoogleTokenRefreshRequest(googleClientId,
                                                                                                   googleClientSecret,
                                                                                                   idpRefreshToken);

        return executeGoogleTokenRefresh(params);
    }

    /**
     * Apple 계정 연동 해제
     * 액세스 토큰 or 리프레시 토큰 관계 없이 사용자의 계정에 대한 서비스 액세스 권한이 취소됨
     *
     * @param tokenType 토큰 유형: access_token, refresh_token
     * @param token     토큰
     */
    public void revokeAppleToken(final TokenType tokenType, final String token) {
        MultiValueMap<String, String> params = OAuthRequestFactory.createAppleTokenRevokeRequest(appleClientId,
                                                                                                 createAppleClientSecret(),
                                                                                                 tokenType,
                                                                                                 token);

        try {
            ResponseEntity<String> response = restClient.post()
                                                        .uri(APPLE_IDP_TOKEN_REVOKE_URL)
                                                        .contentType(APPLICATION_FORM_URLENCODED)
                                                        .body(params)
                                                        .retrieve()
                                                        .toEntity(String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Apple 계정 토큰 폐기 실패: status={}, body={}", response.getStatusCode(), response.getBody());
                throw new GlobalException(SOCIAL_CONNECTION_IDP_TOKEN_REVOKE_FAILED,
                                          "Apple revoke failed: " + response.getBody());
            }
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            log.error("Apple 계정 연동 해지 실패: {}", e.getMessage());
            throw new GlobalException(SOCIAL_CONNECTION_IDP_TOKEN_REVOKE_FAILED, e);
        }
    }

    /**
     * Google 계정 연동 해제
     * 액세스 토큰 or 리프레시 토큰 관계 없이 사용자의 계정에 대한 서비스 액세스 권한이 취소됨
     *
     * @param token 토큰
     */
    public void revokeGoogleToken(final String token) {
        try {
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
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google 계정 연동 해지 실패: {}", e.getMessage());
            throw new GlobalException(SOCIAL_CONNECTION_IDP_TOKEN_REVOKE_FAILED, e);
        }
    }

    // ========================= 내부 메서드 =========================

    /**
     * Apple Client Secret 생성
     *
     * @return Apple 클라이언트 시크릿
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
                       .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 5))    // Apple 인증 코드 만료 시간 5분
                       .signWith(privateKey)
                       .compact();
        } catch (Exception e) {
            log.error("Apple 클라이언트 시크릿 생성 실패: {}", e.getMessage());
            throw new GlobalException(SOCIAL_CONNECTION_IDP_TOKEN_VALIDATION_FAILED);
        }
    }

    /**
     * Apple 계정 인증 API 호출
     *
     * @param params Apple 인증 요청 파라미터
     * @return Apple 인증 요청 결과
     */
    private OAuthResponseDto.AppleTokenValidationResponse executeAppleTokenValidation(
            final MultiValueMap<String, String> params
    ) {
        try {
            return restClient.post()
                             .uri(APPLE_IDP_TOKEN_URL)
                             .contentType(APPLICATION_FORM_URLENCODED)
                             .body(params)
                             .retrieve()
                             .body(AppleTokenValidationResponse.class);
        } catch (Exception e) {
            log.error("Apple 계정 인증 실패 URL: {}, 원인: {}", APPLE_IDP_TOKEN_URL, e.getMessage());
            final ErrorCode errorCode    = SOCIAL_CONNECTION_IDP_TOKEN_VALIDATION_FAILED;
            final String    errorMessage = "Apple 계정 인증 실패\n" + errorCode.getMessage();
            throw new GlobalException(errorCode, errorMessage);
        }
    }

    /**
     * Google 계정 인증 API 호출
     *
     * @param params Google 인증 요청 파라미터
     * @return Google 인증 요청 결과
     */
    private OAuthResponseDto.GoogleTokenValidationResponse executeGoogleTokenValidation(
            final MultiValueMap<String, String> params
    ) {
        try {
            return restClient.post()
                             .uri(GOOGLE_IDP_TOKEN_URL)
                             .contentType(APPLICATION_FORM_URLENCODED)
                             .body(params)
                             .retrieve()
                             .body(GoogleTokenValidationResponse.class);
        } catch (Exception e) {
            log.error("Google 계정 인증 실패 URL: {}, 원인: {}", GOOGLE_IDP_TOKEN_URL, e.getMessage());
            final ErrorCode errorCode    = SOCIAL_CONNECTION_IDP_TOKEN_VALIDATION_FAILED;
            final String    errorMessage = "Google 계정 인증 실패\n" + errorCode.getMessage();
            throw new GlobalException(errorCode, errorMessage);
        }
    }

    /**
     * Apple 계정 토큰 갱신 API 호출
     *
     * @param params Apple 인증 토큰 갱신 요청 파라미터
     * @return Apple 인증 토큰 갱신 요청 결과
     */
    private OAuthResponseDto.AppleTokenRefreshResponse executeAppleTokenRefresh(
            final MultiValueMap<String, String> params
    ) {
        try {
            return restClient.post()
                             .uri(APPLE_IDP_TOKEN_URL)
                             .contentType(APPLICATION_FORM_URLENCODED)
                             .body(params)
                             .retrieve()
                             .body(AppleTokenRefreshResponse.class);
        } catch (Exception e) {
            log.error("Apple 계정 토큰 갱신 실패 URL: {}, 원인: {}", APPLE_IDP_TOKEN_URL, e.getMessage());
            final ErrorCode errorCode    = SOCIAL_CONNECTION_IDP_TOKEN_REFRESH_FAILED;
            final String    errorMessage = "Apple 계정 토큰 갱신 실패\n" + errorCode.getMessage();
            throw new GlobalException(errorCode, errorMessage);
        }
    }

    /**
     * Google 계정 토큰 갱신 API 호출
     *
     * @param params Google 인증 토큰 갱신 요청 파라미터
     * @return Google 인증 토큰 갱신 결과
     */
    private OAuthResponseDto.GoogleTokenRefreshResponse executeGoogleTokenRefresh(
            final MultiValueMap<String, String> params
    ) {
        try {
            return restClient.post()
                             .uri(GOOGLE_IDP_TOKEN_URL)
                             .contentType(APPLICATION_FORM_URLENCODED)
                             .body(params)
                             .retrieve()
                             .body(GoogleTokenRefreshResponse.class);
        } catch (Exception e) {
            log.error("Google 계정 토큰 갱신 실패 URL: {}, 원인: {}", GOOGLE_IDP_TOKEN_URL, e.getMessage());
            final ErrorCode errorCode    = SOCIAL_CONNECTION_IDP_TOKEN_REFRESH_FAILED;
            final String    errorMessage = "Google 계정 토큰 갱신 실패\n" + errorCode.getMessage();
            throw new GlobalException(errorCode, errorMessage);
        }
    }

}
