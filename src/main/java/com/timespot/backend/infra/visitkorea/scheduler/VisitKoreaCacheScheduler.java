package com.timespot.backend.infra.visitkorea.scheduler;

import com.timespot.backend.domain.station.dao.StationRepository;
import com.timespot.backend.domain.station.model.Station;
import com.timespot.backend.infra.visitkorea.service.VisitKoreaPlaceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * PackageName : com.timespot.backend.infra.visitkorea.scheduler
 * FileName    : VisitKoreaCacheScheduler
 * Author      : loadingKKamo21
 * Date        : 26. 4. 5.
 * Description : VisitKorea 캐시 주기적 갱신 스케줄러
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 4. 5.     loadingKKamo21       Initial creation
 */
@Profile("!test")
@Component
@RequiredArgsConstructor
@Slf4j
public class VisitKoreaCacheScheduler {

    private final StationRepository      stationRepository;
    private final VisitKoreaPlaceService visitKoreaPlaceService;

    /**
     * 매일 00:00 에 모든 역의 VisitKorea 캐시를 강제 갱신합니다.
     * <p>
     * GEO 캐시 삭제 → 전체 API 동기화 → PlaceCard 캐시 일괄 저장
     * 이 작업으로 PlaceCard 캐시 (TTL 24 시간) 만료를 사전에 방지합니다.
     * </p>
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void refreshAllPlaceCaches() {
        log.info("========== VisitKorea 캐시 전체 갱신 시작 ==========");

        List<Station> stations = stationRepository.findAll();
        log.info("갱신 대상 역 수: {}", stations.size());

        int successCount = 0;
        int failCount    = 0;

        for (Station station : stations) {
            try {
                log.info("역 캐시 갱신 시작: stationId={}, name={}", station.getId(), station.getName());

                visitKoreaPlaceService.forceSyncPlacesFromVisitKorea(
                        station.getId(),
                        station.getLongitude(),
                        station.getLatitude(),
                        20000
                );

                successCount++;
                log.info("역 캐시 갱신 완료: stationId={}", station.getId());
            } catch (Exception e) {
                failCount++;
                log.error("역 캐시 갱신 실패: stationId={}, error={}",
                          station.getId(), e.getMessage(), e);
            }
        }

        log.info("========== VisitKorea 캐시 전체 갱신 완료: success={}, fail={} ==========",
                 successCount, failCount);
    }

}
