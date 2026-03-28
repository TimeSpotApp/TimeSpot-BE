package com.timespot.backend.domain.device.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import com.timespot.backend.common.model.BaseAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "apns_tokens")
@Getter
@NoArgsConstructor
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
}