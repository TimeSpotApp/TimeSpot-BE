package com.timespot.backend.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * PackageName : com.timespot.backend.common.response
 * FileName    : SuccessCode
 * Author      : loadingKKamo21
 * Date        : 26. 2. 26.
 * Description : 성공 코드 열거형 (공통, 사용자, 장소)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 2. 26.    loadingKKamo21       Initial creation
 * 26. 2. 19.    whitecity01          ADD GET_AVAILABLE_PLACE
 * 26. 3. 22.    whitecity01          Add place detail success
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SuccessCode {

    // Common
    REQUEST_SUCCESS(HttpStatus.OK, "요청이 성공적으로 처리되었습니다."),
    CREATE_SUCCESS(HttpStatus.CREATED, "성공적으로 생성되었습니다."),
    UPDATE_SUCCESS(HttpStatus.OK, "성공적으로 수정되었습니다."),
    DELETE_SUCCESS(HttpStatus.OK, "성공적으로 삭제되었습니다."),

    // User
    USER_REGISTER_SUCCESS(HttpStatus.CREATED, "회원가입이 완료되었습니다."),
    USER_AUTH_LOGIN_SUCCESS(HttpStatus.OK, "로그인 되었습니다."),
    USER_AUTH_LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃 되었습니다."),
    USER_UPDATE_SUCCESS(HttpStatus.OK, "회원정보 수정이 완료되었습니다."),
    USER_WITHDRAW_SUCCESS(HttpStatus.OK, "회원탈퇴가 완료되었습니다."),
    USER_AUTH_TOKEN_REFRESH_SUCCESS(HttpStatus.OK, "인증 토큰 갱신이 완료되었습니다."),
    USER_GET_INFO_SUCCESS(HttpStatus.OK, "회원 정보 조회이 완료되었습니다."),
    USER_MAP_API_UPDATE_SUCCESS(HttpStatus.OK, "회원의 주사용 지도 API 설정이 완료되었습니다."),

    // Place
    PLACE_GET_AVAILABLE_PLACES_SUCCESS(HttpStatus.OK, "방문 가능 장소 조회가 완료되었습니다."),
    PLACE_GET_DETAIL_SUCCESS(HttpStatus.OK, "장소 상세 정보 조회가 완료되었습니다.");

    private final HttpStatus status;
    private final String     message;

}
