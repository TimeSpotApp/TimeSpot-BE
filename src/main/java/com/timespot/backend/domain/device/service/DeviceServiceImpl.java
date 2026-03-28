package com.timespot.backend.domain.device.service;

import static com.timespot.backend.common.response.ErrorCode.USER_NOT_FOUND;

import com.timespot.backend.common.error.GlobalException;
import com.timespot.backend.common.security.model.CustomUserDetails;
import com.timespot.backend.domain.device.dao.ApnsTokenRepository;
import com.timespot.backend.domain.device.dao.UserDeviceRepository;
import com.timespot.backend.domain.device.dto.DeviceRequestDto.DeviceRegisterRequest;
import com.timespot.backend.domain.device.dto.DeviceResponseDto.DeviceRegisterResponse;
import com.timespot.backend.domain.device.model.ApnsToken;
import com.timespot.backend.domain.device.model.UserDevice;
import com.timespot.backend.domain.user.dao.UserRepository;
import com.timespot.backend.domain.user.model.User;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final UserRepository userRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final ApnsTokenRepository apnsTokenRepository;

    @Override
    @Transactional
    public DeviceRegisterResponse registerDevice(final DeviceRegisterRequest dto,
                                                 final CustomUserDetails userDetails) {
        User linkedUser = resolveUser(userDetails);

        Optional<ApnsToken> optionalToken = apnsTokenRepository.findByApnsToken(dto.getDeviceToken());
        if (optionalToken.isPresent()) {
            ApnsToken apnsToken = optionalToken.get();
            UserDevice userDevice = apnsToken.getUserDevice();

            if (linkedUser != null) {
                userDevice.linkUser(linkedUser);
            }

            userDevice.activate();
            apnsToken.activate();

            String responseUserId = userDevice.getUser() != null ? userDevice.getUser().getId().toString() : null;
            return new DeviceRegisterResponse(responseUserId, dto.getDeviceToken(), true);
        }

        UserDevice userDevice = UserDevice.of(linkedUser, createDeviceUuid(dto.getDeviceToken()));
        UserDevice savedUserDevice = userDeviceRepository.save(userDevice);

        ApnsToken apnsToken = ApnsToken.of(savedUserDevice, dto.getDeviceToken());
        apnsTokenRepository.save(apnsToken);

        String responseUserId = linkedUser != null ? linkedUser.getId().toString() : null;
        return new DeviceRegisterResponse(responseUserId, dto.getDeviceToken(), true);
    }

    private User resolveUser(final CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getId() == null) {
            return null;
        }

        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new GlobalException(USER_NOT_FOUND));
    }

    private String createDeviceUuid(final String deviceToken) {
        return UUID.nameUUIDFromBytes(deviceToken.getBytes(StandardCharsets.UTF_8)).toString();
    }
}