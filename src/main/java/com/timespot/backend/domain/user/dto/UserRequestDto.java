package com.timespot.backend.domain.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static lombok.AccessLevel.PRIVATE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
 * 26. 3. 24.    loadingKKamo21       API 문서 상세화 (예시 값 추가)
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(description = "회원 도메인 요청 페이로드")
public abstract class UserRequestDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "회원 정보 수정 요청 페이로드")
    public static class UserInfoUpdateRequest {

        @NotBlank(message = "주사용 지도 API 유형은 필수입니다.")
        @Schema(
                description = """
                              [필수] 주사용 지도 API 유형
                              
                              - `apple`: 애플 지도
                              - `google`: 구글 지도
                              - `naver`: 네이버 지도
                              """,
                example = "apple",
                requiredMode = REQUIRED
        )
        private String mapApi;

    }

}
