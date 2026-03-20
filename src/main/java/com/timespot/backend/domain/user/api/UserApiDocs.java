package com.timespot.backend.domain.user.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.domain.user.dto.UserRequestDto;
import com.timespot.backend.domain.user.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * PackageName : com.timespot.backend.domain.user.api
 * FileName    : UserApiDocs
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@Tag(name = "User API", description = "회원 정보 조회 및 수정, 회원 탈퇴 API")
public interface UserApiDocs {

    @Operation(summary = "회원 정보 조회", description = "현재 사용자의 회원 정보를 조회합니다.")
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 정보 조회 성공"),
            @ApiResponse(responseCode = "400",
                         description = "유효하지 않은 인증 토큰",
                         content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<BaseResponse<UserResponseDto.UserInfoResponse>> getUserInfo(
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(summary = "회원 정보 수정", description = "현재 사용자의 회원 정보를 수정합니다.")
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 정보 수정 성공"),
            @ApiResponse(responseCode = "400",
                         description = "유효하지 않은 인증 토큰",
                         content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<BaseResponse<Void>> updateUserInfo(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "회원 정보 수정 요청 페이로드") UserRequestDto.UserInfoUpdateRequest dto
    );

    @Operation(summary = "회원 탈퇴", description = "현재 사용자를 회원 탈퇴 처리합니다.")
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            @ApiResponse(responseCode = "400",
                         description = "유효하지 않은 인증 토큰",
                         content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<BaseResponse<Void>> withdraw(
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

}
