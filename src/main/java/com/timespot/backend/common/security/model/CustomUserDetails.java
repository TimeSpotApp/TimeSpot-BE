package com.timespot.backend.common.security.model;

import com.timespot.backend.domain.user.model.UserRole;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * PackageName : com.timespot.backend.common.security.model
 * FileName    : CustomUserDetails
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class CustomUserDetails implements UserDetails, OAuth2User {

    @Getter
    private final UUID                id;
    private final String              email;
    @Getter
    private final UserRole            role;
    private final Map<String, Object> attributes;

    public static CustomUserDetails of(final UUID id,
                                       final String email,
                                       final UserRole role) {
        return CustomUserDetails.builder().id(id).email(email).role(role).build();
    }

    public static CustomUserDetails of(final UUID id,
                                       final String email,
                                       final UserRole role,
                                       final Map<String, Object> attributes) {
        return CustomUserDetails.builder().id(id).email(email).role(role).attributes(attributes).build();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.getAuthority()));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return String.valueOf(attributes.get("id"));
    }

}
