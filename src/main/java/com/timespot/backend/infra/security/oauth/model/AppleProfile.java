package com.timespot.backend.infra.security.oauth.model;

import com.timespot.backend.domain.user.model.ProviderType;
import java.util.Map;

/**
 * PackageName : com.timespot.backend.infra.security.oauth.model
 * FileName    : AppleProfile
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
public class AppleProfile extends OAuthProfile {

    protected AppleProfile(final Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getProvider() {
        return ProviderType.APPLE.name();
    }

    @Override
    public String getProviderUserId() {
        return (String) getAttributes().get("sub");
    }

    @Override
    public String getEmail() {
        return (String) getAttributes().get("email");
    }

    @Override
    public String getNickname() {
        // TODO: 애플은 토큰 안에 이름이 없고, 최초 가입 시 클라이언트에게 텍스트로 이름을 전달하기 때문에 별도로 처리해야 함
        return null;
    }

}
