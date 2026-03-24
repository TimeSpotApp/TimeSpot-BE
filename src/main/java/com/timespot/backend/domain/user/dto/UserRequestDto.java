package com.timespot.backend.domain.user.dto;

import static com.timespot.backend.domain.user.constant.UserConst.NICKNAME_REGEX;
import static lombok.AccessLevel.PRIVATE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.user.dto
 * FileName    : UserRequestDto
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description : 사용자 도메인 요청 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(description = "회원 도메인 요청 페이로드")
public abstract class UserRequestDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "회원 정보 수정 요청 페이로드")
    public static class UserInfoUpdateRequest {

        @Pattern(regexp = NICKNAME_REGEX, message = "닉네임은 한글, 영문, 숫자, '-', '_'만 사용하여 2~15자 이내로 입력해주세요.")
        @Schema(description = "[필수] 닉네임")
        private String nickname;

        @NotBlank(message = "주사용 지도 API 유형은 필수입니다.")
        @Schema(description = "[필수] 주사용 지도 API 유형 (예: apple, google, naver)", example = "apple")
        private String mapApi;

    }

}
