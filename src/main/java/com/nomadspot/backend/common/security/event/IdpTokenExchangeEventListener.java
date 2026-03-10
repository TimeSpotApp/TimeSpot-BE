package com.nomadspot.backend.common.security.event;

import com.nomadspot.backend.common.error.GlobalException;
import com.nomadspot.backend.common.response.ErrorCode;
import com.nomadspot.backend.domain.user.dao.SocialConnectionRepository;
import com.nomadspot.backend.domain.user.model.ProviderType;
import com.nomadspot.backend.domain.user.model.SocialConnection;
import com.nomadspot.backend.infra.security.oauth.client.IdpTokenExchangeClient;
import java.net.ConnectException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

/**
 * PackageName : com.nomadspot.backend.common.security.event
 * FileName    : IdpTokenExchangeEventListener
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IdpTokenExchangeEventListener {

    private final SocialConnectionRepository socialConnectionRepository;
    private final IdpTokenExchangeClient     idpTokenExchangeClient;

    @Async
    @Retryable(
            retryFor = {ConnectException.class, HttpServerErrorException.class},
            noRetryFor = {HttpClientErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000L)
    )
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleIdpTokenExchangeEvent(final IdpTokenExchangeEvent event) {
        log.info("비동기 IDP 토큰 교환 시작 - userId: {}, provider: {}", event.getUserId(), event.getProviderType());

        final ProviderType providerType = event.getProviderType();

        String idpRefreshToken = null;

        try {
            switch (providerType) {
                case APPLE -> idpRefreshToken = idpTokenExchangeClient.exchangeAppleAuthCode(event.getAuthCode());
                case GOOGLE -> idpRefreshToken = idpTokenExchangeClient.exchangeGoogleAuthCode(event.getAuthCode());
            }

            if (idpRefreshToken != null) {
                SocialConnection socialConnection = socialConnectionRepository.findByUserId(event.getUserId())
                                                                              .orElseThrow(() -> new GlobalException(
                                                                                      ErrorCode.SOCIAL_CONNECTION_NOT_FOUND
                                                                              ));
                socialConnection.updateIdpRefreshToken(idpRefreshToken);
                log.info("비동기 IDP 토큰 교환 성공 및 DB 저장 완료 - userId: {}", event.getUserId());
            }
        } catch (Exception e) {
            log.error("비동기 IDP 토큰 교환 실패 - userId: {}, 원인: {}", event.getUserId(), e.getMessage());
        }
    }

    @Recover
    public void recover(final Exception e, IdpTokenExchangeEvent event) {
        log.error("IDP 교환 최종 실패 (티켓 소각 또는 3회 재시도 실패) - userId: {}", event.getUserId());
        // TODO: 실패 처리 로직 구현
    }

}
