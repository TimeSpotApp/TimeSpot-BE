package com.timespot.backend.domain.device.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import com.timespot.backend.common.model.BaseAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "apns_tokens")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class ApnsToken extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "apns_token_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_device_id", nullable = false)
    private UserDevice userDevice;

    @Column(name = "apns_token", nullable = false)
    private String apnsToken;

    @Column(name = "is_valid", nullable = false)
    private Boolean isValid;

    @Builder(access = PRIVATE)
    private ApnsToken(final UserDevice userDevice,
                      final String apnsToken,
                      final Boolean isValid) {
        this.userDevice = userDevice;
        this.apnsToken = apnsToken;
        this.isValid = isValid != null ? isValid : true;
    }

    public static ApnsToken of(final UserDevice userDevice,
                               final String apnsToken) {
        return ApnsToken.builder()
                .userDevice(userDevice)
                .apnsToken(apnsToken)
                .isValid(true)
                .build();
    }

    public void relinkUserDevice(final UserDevice userDevice) {
        this.userDevice = userDevice;
    }

    public void activate() {
        this.isValid = true;
    }
}