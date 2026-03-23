package com.timespot.backend.common.security.api;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.security.dto.AuthRequestDto;
import com.timespot.backend.common.security.dto.AuthResponseDto;
import com.timespot.backend.common.security.dto.AuthResponseDto.AuthInfoResponse;
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
 * PackageName : com.timespot.backend.common.security.api
 * FileName    : AuthApiDocs
 * Author      : loadingKKamo21
 * Date        : 26. 3. 9.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 9.     loadingKKamo21       Initial creation
 */
@Tag(name = "Authentication API", description = "OAuth2 로그인, 로그아웃 및 토큰 갱신 API")
public interface AuthApiDocs {

    @Operation(summary = "신규 회원 가입", description = "소셜 인증 제공자 정보를 사용하여 신규 회원 가입을 진행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신규 회원 가입 성공"),
            @ApiResponse(responseCode = "400",
                         description = "유효하지 않은 인증 토큰",
                         content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<BaseResponse<AuthResponseDto.AuthInfoResponse>> signup(
            @Parameter(description = "소셜 인증 제공자로부터 발급받은 1회용 인증 코드 페이로드") AuthRequestDto.OAuth2SignupRequest dto
    );

    @Operation(summary = "OAuth2 소셜 로그인", description = "소셜 인증 제공자의 토큰을 받아 자체 서비스 액세스 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400",
                         description = "유효하지 않은 제공자 또는 토큰",
                         content = @Content(schema = @Schema(hidden = true))),
    })
    ResponseEntity<BaseResponse<AuthResponseDto.AuthInfoResponse>> login(
            @Parameter(description = "소셜 인증 제공자로부터 발급받은 인증 토큰 페이로드") AuthRequestDto.OAuth2LoginRequest dto
    );

    @Operation(summary = "로그아웃", description = "현재 사용자의 액세스 토큰을 만료 처리하고 로그아웃합니다.")
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    ResponseEntity<BaseResponse<Void>> logout(@Parameter(hidden = true) String authorizationHeader);

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 토큰을 발급받습니다.")
    @ApiResponse(responseCode = "200", description = "토큰 갱신 성공")
    ResponseEntity<BaseResponse<AuthInfoResponse>> refresh(
            @Parameter(description = "리프레시 토큰 페이로드") AuthRequestDto.TokenRefreshRequest dto
    );

}
