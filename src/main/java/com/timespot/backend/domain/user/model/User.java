package com.timespot.backend.domain.user.model;

import static com.timespot.backend.common.response.ErrorCode.USER_EMAIL_REQUIRED;
import static com.timespot.backend.common.response.ErrorCode.USER_INVALID_EMAIL_FORMAT;
import static com.timespot.backend.common.response.ErrorCode.USER_INVALID_NICKNAME_FORMAT;
import static com.timespot.backend.common.response.ErrorCode.USER_NICKNAME_REQUIRED;
import static com.timespot.backend.domain.user.constant.UserConst.EMAIL_PATTERN;
import static com.timespot.backend.domain.user.constant.UserConst.NICKNAME_PATTERN;
import static com.timespot.backend.domain.user.model.MapApi.APPLE;
import static com.timespot.backend.domain.user.model.UserRole.USER;
import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static org.hibernate.type.SqlTypes.BINARY;

import com.github.f4b6a3.ulid.UlidCreator;
import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.common.model.BaseAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.springframework.data.domain.Persistable;

/**
 * PackageName : com.timespot.backend.domain.user.model
 * FileName    : User
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class User extends BaseAuditingEntity implements Persistable<UUID> {

    @Id
    @JdbcTypeCode(BINARY)
    @Column(name = "user_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Enumerated(STRING)
    @Column(name = "map_api")
    private MapApi mapApi;

    @Enumerated(STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Builder(access = PRIVATE)
    private User(final String email, final String nickname, final MapApi mapApi, final UserRole role) {
        validateEmail(email);
        validateNickname(nickname);
        this.id = UlidCreator.getUlid().toUuid();
        this.email = email.toLowerCase();
        this.nickname = nickname;
        this.mapApi = mapApi;
        this.role = role != null ? role : USER;
    }

    // ========================= 생성자 메서드 =========================

    public static User of(final String email, final String nickname) {
        return User.builder().email(email).nickname(nickname).mapApi(APPLE).role(USER).build();
    }

    public static User of(final String email, final String nickname, final MapApi mapApi) {
        return User.builder().email(email).nickname(nickname).mapApi(mapApi).role(USER).build();
    }

    public static User of(final String email, final String nickname, final UserRole role) {
        return User.builder().email(email).nickname(nickname).mapApi(APPLE).role(role).build();
    }

    public static User of(final String email, final String nickname, final MapApi mapApi, final UserRole role) {
        return User.builder().email(email).nickname(nickname).mapApi(mapApi).role(role).build();
    }

    // ========================= JPA 엔티티 메서드 =========================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User user)) return false;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Transient
    @Override
    public boolean isNew() {
        return getCreatedAt() == null;
    }

    // ========================= 검증 메서드 =========================

    /**
     * 이메일 검증
     *
     * @param email 이메일
     */
    private void validateEmail(final String email) {
        if (email == null || email.isBlank())
            throw new GlobalException(USER_EMAIL_REQUIRED);
        if (!EMAIL_PATTERN.matcher(email).matches())
            throw new GlobalException(USER_INVALID_EMAIL_FORMAT);
    }

    /**
     * 닉네임 검증
     *
     * @param nickname 닉네임
     */
    private void validateNickname(final String nickname) {
        if (nickname == null || nickname.isBlank())
            throw new GlobalException(USER_NICKNAME_REQUIRED);
        if (!NICKNAME_PATTERN.matcher(nickname).matches())
            throw new GlobalException(USER_INVALID_NICKNAME_FORMAT);
    }

    // ========================= 비즈니스 메서드 =========================

    /**
     * 닉네임 업데이트
     *
     * @param newNickname 새로운 닉네임
     */
    public void updateNickname(final String newNickname) {
        validateNickname(newNickname);
        this.nickname = newNickname;
    }

    /**
     * 지도 API 업데이트
     *
     * @param mapApi 새로운 지도 API 유형
     */
    public void updateMapApi(final MapApi mapApi) {
        this.mapApi = mapApi;
    }

}
