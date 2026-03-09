package com.nomadspot.backend.common.security.event;

import com.nomadspot.backend.common.error.GlobalException;
import com.nomadspot.backend.common.response.ErrorCode;
import com.nomadspot.backend.domain.user.dao.SocialConnectionRepository;
import com.nomadspot.backend.domain.user.model.ProviderType;
import com.nomadspot.backend.domain.user.model.SocialConnection;
import com.nomadspot.backend.infra.security.oauth.client.IdpTokenExchangeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleIdpTokenExchangeEvent(final IdpTokenExchangeEvent event) {
        log.info("비동기 IDP 토큰 교환 시작 - userId: {}, provider: {}", event.getUserId(), event.getProviderType());

        try {
            String idpRefreshToken = null;

            if (ProviderType.APPLE == event.getProviderType())
                idpRefreshToken = idpTokenExchangeClient.exchangeAppleAuthCode(event.getAuthCode());
            else if (ProviderType.GOOGLE == event.getProviderType())
                idpRefreshToken = idpTokenExchangeClient.exchangeGoogleAuthCode(event.getAuthCode());

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

}
