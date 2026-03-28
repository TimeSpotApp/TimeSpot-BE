package com.timespot.backend.domain.device.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import com.timespot.backend.common.model.BaseAuditingEntity;
import com.timespot.backend.domain.user.model.User;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "user_devices")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class UserDevice extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "user_device_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_uuid", nullable = false)
    private String deviceUuid;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Builder(access = PRIVATE)
    private UserDevice(final User user,
                       final String deviceUuid,
                       final Boolean isActive,
                       final LocalDateTime lastSeenAt) {
        this.user = user;
        this.deviceUuid = deviceUuid;
        this.isActive = isActive != null ? isActive : true;
        this.lastSeenAt = lastSeenAt;
    }

    public static UserDevice of(final User user,
                                final String deviceUuid) {
        return UserDevice.builder()
                .user(user)
                .deviceUuid(deviceUuid)
                .isActive(true)
                .lastSeenAt(LocalDateTime.now())
                .build();
    }

    public void linkUser(final User user) {
        this.user = user;
    }

    public void activate() {
        this.isActive = true;
        this.lastSeenAt = LocalDateTime.now();
    }
}