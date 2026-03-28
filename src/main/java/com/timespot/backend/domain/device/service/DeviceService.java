package com.timespot.backend.domain.device.service;

import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.domain.device.dto.DeviceRequestDto.DeviceRegisterRequest;
import com.timespot.backend.domain.device.dto.DeviceResponseDto.DeviceRegisterResponse;

public interface DeviceService {

    DeviceRegisterResponse registerDevice(DeviceRegisterRequest dto,
                                          CustomUserDetails userDetails);
}