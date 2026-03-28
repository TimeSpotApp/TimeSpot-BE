package com.timespot.backend.domain.device.dao;

import com.timespot.backend.domain.device.model.ApnsToken;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ApnsTokenRepository extends JpaRepository<ApnsToken, Long> {

    @Query("""
            select distinct token.apnsToken
            from ApnsToken token
            where token.userDevice.user.id = :userId
              and token.userDevice.isActive = true
              and token.isValid = true
            """)
    List<String> findActiveApnsTokensByUserId(@Param("userId") UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update ApnsToken token
            set token.isValid = false
            where token.apnsToken = :apnsToken
            """)
    int invalidateToken(@Param("apnsToken") String apnsToken);
}