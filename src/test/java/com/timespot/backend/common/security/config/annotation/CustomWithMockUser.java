package com.timespot.backend.common.security.config.annotation;

import com.timespot.backend.common.security.config.CustomWithSecurityContextFactory;
import com.timespot.backend.domain.user.model.MapApi;
import com.timespot.backend.domain.user.model.UserRole;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = CustomWithSecurityContextFactory.class)
public @interface CustomWithMockUser {

    String id() default "019cef6c-6611-9fa8-cb33-a7f6487e4035";

    String email() default "test@example.com";

    String nickname() default "test";

    MapApi mapApi() default MapApi.APPLE;

    UserRole role() default UserRole.USER;

}
