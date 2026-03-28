package com.timespot.backend.domain.device.dao;

import com.timespot.backend.domain.device.model.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
}