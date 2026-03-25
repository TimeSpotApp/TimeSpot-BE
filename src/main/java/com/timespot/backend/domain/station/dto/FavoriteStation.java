package com.timespot.backend.domain.station.dto;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * PackageName : com.timespot.backend.domain.station.dto
 * FileName    : FavoriteStation
 * Author      : whitecity01
 * Date        : 26. 3. 26.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 26.     whitecity01       Initial creation
 */
@Entity
@Table(name = "favorite_stations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriteStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_station_id")
    private Long id;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "user_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID userId;

    @Column(name = "station_id", nullable = false)
    private Long stationId;

    public FavoriteStation(UUID userId, Long stationId) {
        this.userId = userId;
        this.stationId = stationId;
    }
}
