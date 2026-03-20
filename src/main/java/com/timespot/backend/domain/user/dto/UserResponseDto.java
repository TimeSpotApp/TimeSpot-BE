package com.timespot.backend.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.querydsl.core.annotations.QueryProjection;
import com.timespot.backend.domain.user.model.ProviderType;
import com.timespot.backend.domain.user.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.user.dto
 * FileName    : UserResponseDto
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "회원 도메인 응답 페이로드")
public abstract class UserResponseDto {

    @Getter
    @Schema(description = "회원 정보 응답 페이로드")
    public static class UserInfoResponse {

        @Schema(description = "회원 ID")
        private final String        userId;
        @Schema(description = "이메일")
        private final String        email;
        @Schema(description = "닉네임")
        private final String        nickname;
        @Schema(description = "계정 유형: USER, ADMIN")
        private final String        role;
        @Schema(description = "소셜 인증 제공자 유형: APPLE, GOOGLE")
        private final String        providerType;
        @Schema(description = "가입일시")
        private final LocalDateTime createdAt;

        @QueryProjection
        @JsonCreator
        public UserInfoResponse(final UUID userId,
                                final String email,
                                final String nickname,
                                final UserRole role,
                                final ProviderType providerType,
                                final LocalDateTime createdAt) {
            this.userId = userId != null ? userId.toString() : null;
            this.email = email;
            this.nickname = nickname;
            this.role = role != null ? role.name() : null;
            this.providerType = providerType != null ? providerType.name() : null;
            this.createdAt = createdAt;
        }

    }

}
