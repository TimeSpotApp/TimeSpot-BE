package com.timespot.backend.domain.place.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.place.constant
 * FileName    : PlaceConst
 * Author      : whitecity01
 * Date        : 26. 3. 19.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 19.     whitecity01       Initial creation
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class PlaceConst {

    // 도보 속도 (분당 60m)
    public static final int WALK_SPEED_PER_MINUTE = 60;

    // 플랫폼 대기 시간 (10분)
    public static final int PLATFORM_WAIT_TIME = 10;

    // 최소 장소 체류 시간 (10분)
    public static final int MINIMUM_STAY_TIME = 10;

    // 합산된 여유 시간 (20분)
    public static final int TOTAL_BUFFER_TIME = PLATFORM_WAIT_TIME + MINIMUM_STAY_TIME;

}
