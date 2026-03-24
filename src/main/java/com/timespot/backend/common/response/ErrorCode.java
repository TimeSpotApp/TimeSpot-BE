package com.timespot.backend.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * PackageName : com.timespot.backend.common.response
 * FileName    : ErrorCode
 * Author      : loadingKKamo21
 * Date        : 26. 2. 26.
 * Description : 에러 코드 열거형 (공통, 사용자, 소셜 연결, 역, 장소)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 2. 26.    loadingKKamo21       Initial creation
 * 26. 3. 19.    whitecity01          Add Station, Place error
 * 26. 3. 22.    whitecity01          Add place detail error
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "CO001", "유효하지 않은 입력 값입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "CO002", "유효하지 않은 타입 값입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "CO003", "해당 엔티티를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "CO004", "지원하지 않는 HTTP Method 입니다."),
    METHOD_NOT_SUPPORTED(HttpStatus.METHOD_NOT_ALLOWED, "CO005", "지원하지 않는 Content-Type 입니다."),
    METHOD_ARGUMENT_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "CO006", "요청 인자의 타입이 올바르지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "CO007", "접근 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "CO008", "인증 정보가 유효하지 않습니다."),
    CONSTRAINT_VIOLATION(HttpStatus.CONFLICT, "CO009", "데이터베이스 제약 조건 위반입니다."),
    BAD_SQL_GRAMMAR(HttpStatus.INTERNAL_SERVER_ERROR, "CO010", "잘못된 SQL 문법 오류가 발생했습니다."),
    REQUEST_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "CO011", "요청의 크기가 너무 큽니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "CO012", "너무 많은 요청을 보냈습니다. 잠시 후 다시 시도해주세요."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "CO013", "요청하신 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CO999", "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "US001", "해당 회원을 찾을 수 없습니다."),
    USER_EMAIL_DUPLICATED(HttpStatus.BAD_REQUEST, "US002", "이미 사용 중인 이메일 주소입니다."),
    USER_ALREADY_WITHDRAWN(HttpStatus.CONFLICT, "US003", "이미 탈퇴한 회원입니다."),
    USER_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "US004", "이메일은 필수입니다."),
    USER_INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "US005", "이메일 형식이 올바르지 않습니다."),
    USER_NICKNAME_REQUIRED(HttpStatus.BAD_REQUEST, "US006", "닉네임은 필수입니다."),
    USER_INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST, "US007",
                                 "닉네임 형식이 올바르지 않습니다. 2~15자 영문, 한글, 숫자, '-', '_'만 사용 가능합니다."),
    USER_AUTH_INVALID_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "US008", "잘못된 AccessToken입니다."),
    USER_AUTH_INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "US009", "잘못된 RefreshToken입니다."),
    USER_AUTH_ACCESS_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "US010", "AccessToken이 만료되었습니다."),
    USER_AUTH_REFRESH_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "US011", "RefreshToken이 만료되었습니다."),
    USER_ROLE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "US012", "지원하지 않는 계정 유형입니다."),
    USER_MAP_API_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "US013", "지원하지 않는 지도 API 유형입니다."),

    // Social Connection
    SOCIAL_CONNECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SO001", "소셜 연동 정보를 찾을 수 없습니다."),
    SOCIAL_CONNECTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "SO002", "이미 사용 중인 소셜 연동 정보가 존재합니다."),
    SOCIAL_CONNECTION_USER_MUST_NOT_BE_NULL(HttpStatus.BAD_REQUEST, "SO003", "회원 정보는 필수입니다."),
    SOCIAL_CONNECTION_INVALID_PROVIDER_TYPE(HttpStatus.BAD_REQUEST, "SO004", "소셜 인증 제공자 유형이 올바르지 않습니다."),
    SOCIAL_CONNECTION_INVALID_PROVIDER_ID(HttpStatus.BAD_REQUEST, "SO005", "소셜 인증 제공자 ID가 올바르지 않습니다."),
    SOCIAL_CONNECTION_INVALID_IDP_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "SO006", "소셜 인증 제공자 Refresh Token이 올바르지 않습니다."),
    SOCIAL_CONNECTION_PROVIDER_NOT_SUPPORTED(HttpStatus.INTERNAL_SERVER_ERROR, "SO007", "지원하지 않는 소셜 인증 제공자 유형입니다."),
    SOCIAL_CONNECTION_TOKEN_PARSE_FAILED(HttpStatus.BAD_REQUEST, "SO008", "소셜 인증 토큰 파싱에 실패했습니다."),
    SOCIAL_CONNECTION_INVALID_TOKEN(HttpStatus.BAD_REQUEST, "SO009", "잘못되거나 만료된 소셜 인증 토큰입니다."),
    SOCIAL_CONNECTION_IDP_TOKEN_VALIDATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SO010", "IDP 인증에 실패했습니다."),
    SOCIAL_CONNECTION_IDP_TOKEN_REFRESH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SO011", "IDP 인증 토큰 갱신에 실패했습니다."),
    SOCIAL_CONNECTION_IDP_TOKEN_REVOKE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SO012", "IDP 인증 토큰 폐기에 실패했습니다."),

    // Station
    STATION_NOT_FOUND(HttpStatus.NOT_FOUND, "ST001", "역을 찾을 수 없습니다."),
    STATION_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "ST002", "현재 비활성화된 역입니다."),

    // Place
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PL001", "장소를 찾을 수 없습니다."),
    PLACE_INSUFFICIENT_REMAINING_TIME(HttpStatus.BAD_REQUEST, "PL002", "체류 가능한 시간이 부족합니다.");

    private final HttpStatus status;
    private final String     code;
    private final String     message;

}
