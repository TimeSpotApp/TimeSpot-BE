package com.timespot.backend.domain.place.model;

import com.timespot.backend.common.model.BaseAuditingEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * PackageName : com.timespot.backend.domain.place.model
 * FileName    : Place
 * Author      : whitecity01
 * Date        : 26. 3. 19.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 19.     whitecity01       Initial creation
 * 26. 3. 20.     whitecity01       REFACTOR DATE INIT
 */
@Getter
@Entity
@Table(name = "places")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "google_place_id", nullable = false)
    private String googlePlaceId;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 50)
    @NotNull
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Size(max = 255)
    @NotNull
    @Column(name = "address", nullable = false)
    private String address;

    // ========================= JPA 엔티티 메서드 =========================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Place place)) return false;
        return getId() != null && Objects.equals(getId(), place.getId());
    }

    @Override
    public int hashCode() { return Objects.hashCode(getId()); }
}