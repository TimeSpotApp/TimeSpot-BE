package com.timespot.backend.domain.place.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

/**
 * PackageName : com.timespot.backend.domain.place.dto
 * FileName    : GooglePlaceDto
 * Author      : whitecity01
 * Date        : 26. 3. 22.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 22.     whitecity01       ADD place detail
 */
public class GooglePlaceDto {

    @Getter
    public static class Response {
        private String internationalPhoneNumber;
        private RegularOpeningHours regularOpeningHours;
        private List<Photo> photos;
    }

    @Getter
    public static class RegularOpeningHours {
        private List<String> weekdayDescriptions;
    }

    @Getter
    public static class Photo {
        private String name;
    }

    @Getter
    @Builder
    public static class ParsedResult {
        private boolean isSuccess;
        private String phoneNumber;
        private List<String> weekdayHours;
        private List<String> weekendHours;
        private List<String> imageUrls;
    }
}