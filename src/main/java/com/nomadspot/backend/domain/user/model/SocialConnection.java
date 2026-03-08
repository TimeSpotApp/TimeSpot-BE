package com.nomadspot.backend.domain.user.model;

import com.nomadspot.backend.common.error.GlobalException;
import com.nomadspot.backend.common.model.BaseAuditingEntity;
import com.nomadspot.backend.common.response.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.nomadspot.backend.domain.user.model
 * FileName    : SocialConnection
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
@Entity
@Table(name = "social_connections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SocialConnection extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_connection_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, updatable = false)
    private ProviderType providerType;

    @Column(name = "provider_id", nullable = false, updatable = false)
    private String providerId;

    @Column(name = "idp_refresh_token")
    private String idpRefreshToken;

    @Builder(access = AccessLevel.PRIVATE)
    private SocialConnection(final User user,
                             final ProviderType providerType,
                             final String providerId,
                             final String idpRefreshToken) {
        validateProviderId(providerId);
        this.user = user;
        this.providerType = providerType != null ? providerType : ProviderType.APPLE;
        this.providerId = providerId;
        this.idpRefreshToken = idpRefreshToken;
    }

    // ========================= 생성자 메서드 =========================

    public static SocialConnection of(final User user, final ProviderType providerType, final String providerId) {
        return SocialConnection.builder().user(user).providerType(providerType).providerId(providerId).build();
    }

    public static SocialConnection of(final User user,
                                      final ProviderType providerType,
                                      final String providerId,
                                      final String idpRefreshToken) {
        return SocialConnection.builder()
                               .user(user)
                               .providerType(providerType)
                               .providerId(providerId)
                               .idpRefreshToken(idpRefreshToken)
                               .build();
    }

    // ========================= 검증 메서드 =========================

    /**
     * 소셜 인증 고유 식별자 검증
     *
     * @param providerId 소셜 인증 고유 식별자
     */
    private void validateProviderId(final String providerId) {
        if (providerId == null || providerId.isBlank())
            throw new GlobalException(ErrorCode.SOCIAL_CONNECTION_INVALID_PROVIDER_ID);
    }

    // ========================= 비즈니스 메서드 =========================

    /**
     * 소셜 인증 제공자 Refresh Token 업데이트
     *
     * @param newIdpRefreshToken 새로운 소셜 인증 제공자 Refresh Token
     */
    public void updateIdpRefreshToken(final String newIdpRefreshToken) {
        this.idpRefreshToken = newIdpRefreshToken;
    }

}
