package com.timespot.backend.domain.user.dto;

import com.timespot.backend.domain.user.constant.UserConst;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.user.dto
 * FileName    : UserRequestDto
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "회원 도메인 요청 페이로드")
public abstract class UserRequestDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "회원 정보 수정 요청 페이로드")
    public static class UserInfoUpdateRequest {

        @Pattern(regexp = UserConst.NICKNAME_REGEX, message = "닉네임은 한글, 영문, 숫자, '-', '_'만 사용하여 2~15자 이내로 입력해주세요.")
        @Schema(description = "[필수] 신규 닉네임")
        private String newNickname;

    }

}
