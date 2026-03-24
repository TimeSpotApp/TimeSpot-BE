package com.timespot.backend.common.security.config.annotation;

import static com.timespot.backend.domain.user.model.UserRole.USER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.timespot.backend.common.security.config.CustomWithSecurityContextFactory;
import com.timespot.backend.domain.user.model.MapApi;
import com.timespot.backend.domain.user.model.ProviderType;
import com.timespot.backend.domain.user.model.UserRole;
import java.lang.annotation.Retention;
import org.springframework.security.test.context.support.WithSecurityContext;

/**
 * PackageName : com.timespot.backend.common.security.config.annotation
 * FileName    : CustomWithMockUser
 * Author      : loadingKKamo21
 * Date        : 26. 3. 15.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 15.    loadingKKamo21       Initial creation
 */
@Retention(RUNTIME)
@WithSecurityContext(factory = CustomWithSecurityContextFactory.class)
public @interface CustomWithMockUser {

    String id() default "019cef6c-6611-9fa8-cb33-a7f6487e4035";

    String email() default "test@example.com";

    String nickname() default "test";

    ProviderType providerType() default ProviderType.APPLE;

    MapApi mapApi() default MapApi.APPLE;

    UserRole role() default USER;

}
