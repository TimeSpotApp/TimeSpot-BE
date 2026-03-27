package com.timespot.backend.domain.device.api;

import static com.timespot.backend.common.response.SuccessCode.DEVICE_REGISTER_SUCCESS;
import static com.timespot.backend.common.response.SuccessCode.DEVICE_UNREGISTER_SUCCESS;

import com.timespot.backend.common.response.BaseResponse;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.domain.device.dto.DeviceRequestDto.DeviceRegisterRequest;
import com.timespot.backend.domain.device.dto.DeviceResponseDto.DeviceRegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PackageName : com.timespot.backend.domain.device.api
 * FileName    : DeviceController
 * Author      : loadingKKamo21
 * Date        : 26. 3. 27.
 * Description : 디바이스 API 컨트롤러 (등록/삭제)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 27.    loadingKKamo21       Initial creation
 */
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController implements DeviceApiDocs {

    // TODO: 서비스 계층 개발 시 주석 해제
    // private final DeviceService deviceService;

    @PostMapping
    @Override
    public ResponseEntity<BaseResponse<DeviceRegisterResponse>> registerDevice(
            @RequestBody final DeviceRegisterRequest dto,
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        // TODO: 서비스 계층 개발 시 구현
        // DeviceRegisterResponse responseData = deviceService.registerDevice(dto, userDetails);

        // 임시 응답 (서비스 계층 개발 시 제거)
        DeviceRegisterResponse responseData = new DeviceRegisterResponse(
                1L,
                userDetails != null ? userDetails.getId().toString() : null,
                dto.getDeviceToken(),
                dto.getPlatform(),
                true,
                java.time.LocalDateTime.now()
        );

        return ResponseEntity.ok(BaseResponse.success(DEVICE_REGISTER_SUCCESS, responseData));
    }

    @DeleteMapping
    @Override
    public ResponseEntity<BaseResponse<Void>> unregisterDevice(
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        // TODO: 서비스 계층 개발 시 구현
        // deviceService.unregisterDevice(userDetails);

        return ResponseEntity.ok(BaseResponse.success(DEVICE_UNREGISTER_SUCCESS));
    }

}
