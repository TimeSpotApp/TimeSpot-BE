package com.timespot.backend.domain.place.dao;

import com.timespot.backend.domain.place.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * PackageName : com.timespot.backend.domain.place.dao
 * FileName    : StationRepository
 * Author      : whitecity01
 * Date        : 26. 3. 19.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 19.     whitecity01       Initial creation
 */
public interface StationRepository extends JpaRepository<Station, Long> {
    Optional<Station> findByIdAndIsActiveTrue(Long id);
}