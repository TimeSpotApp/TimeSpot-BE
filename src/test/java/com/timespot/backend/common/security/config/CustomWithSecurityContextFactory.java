package com.timespot.backend.common.security.config;

import com.timespot.backend.common.security.config.annotation.CustomWithMockUser;
import com.timespot.backend.common.security.model.CustomUserDetails;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

/**
 * PackageName : com.timespot.backend.common.security.config
 * FileName    : CustomWithSecurityContextFactory
 * Author      : loadingKKamo21
 * Date        : 26. 3. 15.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 15.    loadingKKamo21       Initial creation
 */
public class CustomWithSecurityContextFactory implements WithSecurityContextFactory<CustomWithMockUser> {

    @Override
    public SecurityContext createSecurityContext(CustomWithMockUser annotation) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

        UserDetails userDetails = CustomUserDetails.of(UUID.fromString(annotation.id()),
                                                       annotation.email(),
                                                       annotation.mapApi(),
                                                       annotation.role());

        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(userDetails,
                                                                                  null,
                                                                                  userDetails.getAuthorities()));

        return securityContext;
    }

}
