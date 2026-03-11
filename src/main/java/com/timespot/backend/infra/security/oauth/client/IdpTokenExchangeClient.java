package com.timespot.backend.infra.security.oauth.client;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.common.response.ErrorCode;
import com.timespot.backend.infra.security.oauth.constant.OAuthConst;
import com.timespot.backend.infra.security.oauth.properties.OAuth2Properties;
import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
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

    private final String googleClientId;
    private final String googleClientSecret;

    public IdpTokenExchangeClient(final RestClient.Builder builder, final OAuth2Properties oAuth2Properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(5).toMillis());

        restClient = builder.requestFactory(factory).build();

        appleClientId = oAuth2Properties.getApple().getClientId();
        appleTeamId = oAuth2Properties.getApple().getTeamId();
        appleKeyId = oAuth2Properties.getApple().getKeyId();
        applePrivateKey = oAuth2Properties.getApple().getPrivateKey();

        googleClientId = oAuth2Properties.getGoogle().getClientId();
        googleClientSecret = oAuth2Properties.getGoogle().getClientSecret();
    }

    /**
     * Google 인증 코드 교환
     *
     * @param authCode Google 인증 코드
     * @return Google 인증 토큰
     */
    public String exchangeGoogleAuthCode(final String authCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("code", authCode);
        params.add("redirect_uri", OAuthConst.GOOGLE_IDP_TOKEN_REDIRECT_URL);

        return executeExchange(OAuthConst.GOOGLE_IDP_TOKEN_EXCHANGE_URL, params);
    }

    /**
     * Apple 인증 코드 교환
     *
     * @param authCode Apple 인증 코드
     * @return Apple 인증 토큰
     */
    public String exchangeAppleAuthCode(final String authCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", appleClientId);
        params.add("client_secret", createAppleClientSecret());
        params.add("code", authCode);

        return executeExchange(OAuthConst.APPLE_IDP_TOKEN_EXCHANGE_URL, params);
    }

    // ========================= 내부 메서드 =========================

    /**
     * IDP 인증 토큰 교환
     *
     * @param url    IDP 인증 토큰 교환 URL
     * @param params IDP 인증 토큰 교환 파라미터
     * @return IDP 인증 토큰
     */
    private String executeExchange(final String url, final MultiValueMap<String, String> params) {
        try {
            Map<String, Object> response = restClient.post()
                                                     .uri(url)
                                                     .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                                     .body(params)
                                                     .retrieve()
                                                     .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response != null && response.containsKey("refresh_token"))
                return (String) response.get("refresh_token");
            else {
                log.warn("IDP 응답에 refresh_token이 포함되어 있지 않습니다.");
                return null;
            }
        } catch (Exception e) {
            log.error("IDP Token 교환 실패 URL: {}, 원인: {}", url, e.getMessage());
            throw new GlobalException(ErrorCode.SOCIAL_CONNECTION_IDP_TOKEN_EXCHANGE_FAILED);
        }
    }

    /**
     * Apple Client Secret 생성
     *
     * @return Apple Client Secret
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
                       .audience().add(OAuthConst.APPLE_ISSUER).and()
                       .subject(appleClientId)
                       .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 5))
                       .signWith(privateKey)
                       .compact();
        } catch (Exception e) {
            log.error("Apple Client Secret 생성 실패: {}", e.getMessage());
            throw new GlobalException(ErrorCode.SOCIAL_CONNECTION_IDP_TOKEN_EXCHANGE_FAILED);
        }
    }

}
