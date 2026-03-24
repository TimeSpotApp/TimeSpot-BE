package com.timespot.backend.domain.user.model;

import static com.timespot.backend.common.response.ErrorCode.SOCIAL_CONNECTION_INVALID_PROVIDER_ID;
import static com.timespot.backend.common.response.ErrorCode.SOCIAL_CONNECTION_PROVIDER_NOT_SUPPORTED;
import static com.timespot.backend.common.response.ErrorCode.SOCIAL_CONNECTION_USER_MUST_NOT_BE_NULL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.common.model.BaseAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.user.model
 * FileName    : SocialConnection
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description : 소셜 연결 엔티티 (사용자 -소셜 인증 제공자 연동 정보)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
@Entity
@Table(name = "social_connections")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class SocialConnection extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "social_connection_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(STRING)
    @Column(name = "provider_type", nullable = false, updatable = false)
    private ProviderType providerType;

    @Column(name = "provider_id", nullable = false, updatable = false)
    private String providerId;

    @Column(name = "idp_refresh_token")
    private String idpRefreshToken;

    @Column(name = "idp_refresh_token_received_at")
    private LocalDateTime idpRefreshTokenReceivedAt;

    @Builder(access = PRIVATE)
    private SocialConnection(final User user,
                             final ProviderType providerType,
                             final String providerId) {
        validateUser(user);
        validateProviderType(providerType);
        validateProviderId(providerId);
        this.user = user;
        this.providerType = providerType;
        this.providerId = providerId;
    }

    // ========================= 생성자 메서드 =========================

    public static SocialConnection of(final User user,
                                      final ProviderType providerType,
                                      final String providerId,
                                      final String idpRefreshToken) {
        SocialConnection socialConnection = SocialConnection.builder()
                                                            .user(user)
                                                            .providerType(providerType)
                                                            .providerId(providerId)
                                                            .build();
        socialConnection.setIdpRefreshToken(idpRefreshToken);
        return socialConnection;
    }

    // ========================= 검증 메서드 =========================

    /**
     * 회원 정보 검증
     *
     * @param user 회원 정보
     */
    private void validateUser(final User user) {
        if (user == null || user.getId() == null || user.getCreatedAt() == null)
            throw new GlobalException(SOCIAL_CONNECTION_USER_MUST_NOT_BE_NULL);
    }

    /**
     * 소셜 인증 제공자 유형 검증
     *
     * @param providerType 소셜 인증 제공자 유형
     */
    private void validateProviderType(final ProviderType providerType) {
        if (providerType == null)
            throw new GlobalException(SOCIAL_CONNECTION_PROVIDER_NOT_SUPPORTED);
    }

    /**
     * 소셜 인증 고유 식별자 검증
     *
     * @param providerId 소셜 인증 고유 식별자
     */
    private void validateProviderId(final String providerId) {
        if (providerId == null || providerId.isBlank())
            throw new GlobalException(SOCIAL_CONNECTION_INVALID_PROVIDER_ID);
    }

    // ========================= 내부 메서드 =========================

    /**
     * 소셜 인증 제공자 IDP Refresh Token 설정
     *
     * @param idpRefreshToken IDP Refresh Token
     */
    private void setIdpRefreshToken(final String idpRefreshToken) {
        this.idpRefreshToken = idpRefreshToken;
        this.idpRefreshTokenReceivedAt = LocalDateTime.now();
    }

}
