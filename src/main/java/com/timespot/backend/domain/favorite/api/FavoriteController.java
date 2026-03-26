package com.timespot.backend.domain.favorite.api;

import static com.timespot.backend.common.response.SuccessCode.FAVORITE_CREATE_SUCCESS;
import static com.timespot.backend.common.response.SuccessCode.FAVORITE_DELETE_SUCCESS;
import static com.timespot.backend.common.response.SuccessCode.FAVORITE_GET_SUCCESS;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.response.annotation.CustomPageResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.common.util.SortUtils;
import com.timespot.backend.domain.favorite.dto.FavoriteRequestDto.FavoriteStationCreateRequest;
import com.timespot.backend.domain.favorite.dto.FavoriteResponseDto.FavoriteListResponse;
import com.timespot.backend.domain.favorite.service.FavoriteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.timespot.backend.domain.favorite.api
 * FileName    : FavoriteController
 * Author      : loadingKKamo21
 * Date        : 26. 3. 25.
 * Description : 즐겨찾기 API 컨트롤러
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 25.    loadingKKamo21       Initial creation
 */
@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController implements FavoriteApiDocs {

    private final FavoriteService favoriteService;

    @PostMapping
    @Override
    public ResponseEntity<BaseResponse<Void>> createFavoriteStation(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @RequestBody @Valid final FavoriteStationCreateRequest dto
    ) {
        favoriteService.createFavoriteStation(userDetails.getId(), dto.getStationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(FAVORITE_CREATE_SUCCESS));
    }

    @DeleteMapping("/{favoriteId}")
    @Override
    public ResponseEntity<BaseResponse<Void>> deleteFavoriteStation(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @PathVariable @Min(1) final Long favoriteId
    ) {
        favoriteService.deleteFavoriteStation(userDetails.getId(), favoriteId);
        return ResponseEntity.ok(BaseResponse.success(FAVORITE_DELETE_SUCCESS));
    }

    @GetMapping
    @CustomPageResponse(
            numberOfElements = false,
            empty = false,
            hasContent = false,
            first = false,
            last = false,
            hasPrevious = false
    )
    @Override
    public ResponseEntity<BaseResponse<Page<FavoriteListResponse>>> getFavoriteStationList(
            @AuthenticationPrincipal final CustomUserDetails userDetails,
            @RequestParam(required = false, defaultValue = "") final String keyword,
            @RequestParam(required = false, defaultValue = "1") final int page,
            @RequestParam(required = false, defaultValue = "10") final int size,
            @RequestParam(required = false, defaultValue = "createdAt,DESC") final String sort
    ) {
        Pageable pageable = SortUtils.createPageable(page, size, sort);
        Page<FavoriteListResponse> responseData = favoriteService.getFavoriteStationList(
                userDetails.getId(), keyword, pageable
        );
        return ResponseEntity.ok(BaseResponse.success(FAVORITE_GET_SUCCESS, responseData));
    }

}
