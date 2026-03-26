package com.timespot.backend.domain.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PackageName : com.timespot.backend.domain.place.dto
 * FileName    : PlaceResponseDto
 * Author      : whitecity01
 * Date        : 26. 3. 19.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 19.     whitecity01       Initial creation
 * 26. 3. 22.     whitecity01       ADD pagenation
 * 26. 3. 22.     whitecity01       ADD place detail
 * 26. 3. 26.     whitecity01       MODIFY findAvailablePlacesOnRoute logic
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "장소 도메인 응답 페이로드")
public abstract class PlaceResponseDto {

    @Schema(description = "방문 가능한 장소 응답 페이로드")
    public interface AvailablePlace {

        @Schema(description = "장소 이름", example = "스타벅스 서울역점")
        String getName();

        @Schema(description = "구글 플레이스 ID", example = "ChIJudLQD1mjfDUR3VmcnRfX3Yg")
        String getGooglePlaceId();

        @Schema(description = "장소 카테고리", example = "카페")
        String getCategory();

        @Schema(description = "도로명 주소", example = "서울특별시 용산구 동자동 11-111")
        String getAddress();

        @Schema(description = "위도", example = "30.1111")
        Double getLat();

        @Schema(description = "경도", example = "120.1111")
        Double getLon();

        @Schema(description = "체류 가능 시간 (분)", example = "25")
        Integer getStayableMinutes();
    }

    public interface PlaceDetailInDB {
        String getName();
        String getCategory();
        String getAddress();
        Double getDistanceToStation();
        Integer getTimeToStation();
    }

    @Getter
    @Builder
    @Schema(description = "장소 상세 정보 응답 페이로드")
    public static class PlaceDetail {
        @Schema(description = "장소 이름", example = "스타벅스 서울역점")
        private String name;

        @Schema(description = "장소 카테고리 (구글 기준)", example = "카페")
        private String category;

        @Schema(description = "도로명 주소", example = "서울특별시 용산구 동자동 11-111")
        private String address;

        @Schema(description = "역으로부터의 직선 거리 (미터)", example = "450.5")
        private Double distanceToStation;

        @Schema(description = "역에서부터 도보 소요 시간 (분)", example = "5")
        private Integer timeToStation;

        // Google API 연동 데이터
        @Schema(description = "장소 대표 이미지 URL", example = "https://places.googleapis.com/v1/places/ChIJ.../media?key=...&maxWidthPx=400")
        private String imageUrl;

        @Schema(description = "평일 영업시간 목록", example = "[\"월요일: AM 09:00 ~ PM 06:00\", \"화요일: AM 09:00 ~ PM 06:00\"]")
        private List<String> weekday;

        @Schema(description = "주말 영업시간 목록", example = "[\"토요일: AM 10:00 ~ PM 08:00\", \"일요일: 휴무\"]")
        private List<String> weekend;

        @Schema(description = "국제 전화번호", example = "+82 2-1234-5678")
        private String phoneNumber;
    }
}