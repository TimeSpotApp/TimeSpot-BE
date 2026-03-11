package com.timespot.backend.common.security.event;

import com.timespot.backend.domain.user.model.ProviderType;
import java.util.UUID;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * PackageName : com.timespot.backend.common.security.event
 * FileName    : IdpTokenExchangeEvent
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@Getter
public class IdpTokenExchangeEvent extends ApplicationEvent {

    private final UUID         userId;
    private final ProviderType providerType;
    private final String       authCode;

    public IdpTokenExchangeEvent(final Object source,
                                 final UUID userId,
                                 final ProviderType providerType,
                                 final String authCode) {
        super(source);
        this.userId = userId;
        this.providerType = providerType;
        this.authCode = authCode;
    }

}
