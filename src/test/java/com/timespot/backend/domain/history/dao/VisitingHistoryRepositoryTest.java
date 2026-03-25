package com.timespot.backend.domain.history.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import autoparams.AutoSource;
import autoparams.Repeat;
import com.timespot.backend.common.config.EnableJpaAuditingConfig;
import com.timespot.backend.common.config.P6SpyConfig;
import com.timespot.backend.common.config.QuerydslConfig;
import com.timespot.backend.common.util.TestUtils;
import com.timespot.backend.domain.history.dto.VisitingHistoryResponseDto.VisitingHistoryListResponse;
import com.timespot.backend.domain.history.model.VisitingHistory;
import com.timespot.backend.domain.place.model.Place;
import com.timespot.backend.domain.place.model.Station;
import com.timespot.backend.domain.user.model.User;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * PackageName : com.timespot.backend.domain.history.dao
 * FileName    : VisitingHistoryRepositoryTest
 * Author      : loadingKKamo21
 * Date        : 26. 3. 25.
 * Description : 방문 이력 리포지토리 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 25.    loadingKKamo21       Initial creation
 */
@DataJpaTest
@Import({EnableJpaAuditingConfig.class, P6SpyConfig.class, QuerydslConfig.class})
class VisitingHistoryRepositoryTest {

    @Autowired
    private TestEntityManager         em;
    @Autowired
    private VisitingHistoryRepository visitingHistoryRepository;

    @Nested
    @DisplayName("save() 테스트")
    class SaveTests {

        @RepeatedTest(10)
        @DisplayName("VisitingHistory 엔티티 저장 (진행 중)")
        void save_inProgress() {
            // given
            User            user            = em.persistAndFlush(TestUtils.createUser());
            Station         station         = em.persistAndFlush(TestUtils.createStation());
            Place           place           = em.persistAndFlush(TestUtils.createPlace());
            LocalDateTime   startTime       = LocalDateTime.now();
            VisitingHistory visitingHistory = TestUtils.createVisitingHistory(user, station, place, startTime);

            // when
            Long id = visitingHistoryRepository.save(visitingHistory).getId();
            em.flush();

            // then
            VisitingHistory savedHistory = em.find(VisitingHistory.class, id);

            assertNotNull(savedHistory, "savedHistory 는 null 이 아니어야 합니다.");
            assertEquals(visitingHistory.getUser(), savedHistory.getUser(), "user 는 같아야 합니다.");
            assertEquals(visitingHistory.getStation(), savedHistory.getStation(), "station 은 같아야 합니다.");
            assertEquals(visitingHistory.getStartTime(), savedHistory.getStartTime(), "startTime 은 같아야 합니다.");
            assertEquals(visitingHistory.getTrainDepartureTime(), savedHistory.getTrainDepartureTime(),
                         "trainDepartureTime 은 같아야 합니다.");
            assertNull(savedHistory.getEndTime(), "endTime 은 null 이어야 합니다 (진행 중).");
            assertFalse(savedHistory.isSuccess(), "isSuccess 는 false 여야 합니다 (진행 중).");
        }

        @RepeatedTest(10)
        @DisplayName("VisitingHistory 엔티티 저장 (완료)")
        void save_completed() {
            // given
            User            user            = em.persistAndFlush(TestUtils.createUser());
            Station         station         = em.persistAndFlush(TestUtils.createStation());
            Place           place           = em.persistAndFlush(TestUtils.createPlace());
            LocalDateTime   startTime       = LocalDateTime.now();
            VisitingHistory visitingHistory = TestUtils.createVisitingHistory(user, station, place, startTime);

            // when
            Long id = visitingHistoryRepository.save(visitingHistory).getId();
            em.flush();

            // then
            VisitingHistory savedHistory = em.find(VisitingHistory.class, id);

            assertNotNull(savedHistory, "savedHistory 는 null 이 아니어야 합니다.");
            assertEquals(visitingHistory.getUser(), savedHistory.getUser(), "user 는 같아야 합니다.");
            assertEquals(visitingHistory.getStation(), savedHistory.getStation(), "station 은 같아야 합니다.");
            assertEquals(visitingHistory.getStartTime(), savedHistory.getStartTime(), "startTime 은 같아야 합니다.");
            assertEquals(visitingHistory.getEndTime(), savedHistory.getEndTime(), "endTime 은 같아야 합니다.");
            assertNotNull(savedHistory.getTotalDurationMinutes(), "totalDurationMinutes 는 null 이 아니어야 합니다.");
        }

    }

    @Nested
    @DisplayName("findById() 테스트")
    class FindByIdTests {

        @RepeatedTest(10)
        @DisplayName("id 로 VisitingHistory 엔티티 단 건 조회")
        void findById() {
            // given
            User          user      = em.persistAndFlush(TestUtils.createUser());
            Station       station   = em.persistAndFlush(TestUtils.createStation());
            Place         place     = em.persistAndFlush(TestUtils.createPlace());
            LocalDateTime startTime = LocalDateTime.now();
            VisitingHistory visitingHistory = em.persistAndFlush(
                    TestUtils.createVisitingHistory(user, station, place, startTime)
            );
            Long id = visitingHistory.getId();

            // when
            VisitingHistory findHistory = visitingHistoryRepository.findById(id).get();

            // then
            assertNotNull(findHistory, "findHistory 는 null 이 아니어야 합니다.");
            assertEquals(visitingHistory.getUser(), findHistory.getUser(), "user 는 같아야 합니다.");
            assertEquals(visitingHistory.getStation(), findHistory.getStation(), "station 은 같아야 합니다.");
            assertEquals(visitingHistory.getStartTime(), findHistory.getStartTime(), "startTime 은 같아야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("존재하지 않는 id 로 VisitingHistory 엔티티 단 건 조회 시도")
        void findById_unknownId(@Min(1) final long unknownId) {
            // when
            Optional<VisitingHistory> opHistory = visitingHistoryRepository.findById(unknownId);

            // then
            assertFalse(opHistory.isPresent(), "조회된 VisitingHistory 엔티티가 존재하지 않아야 합니다.");
        }

    }

    @Nested
    @DisplayName("deleteById() 테스트")
    class DeleteByIdTests {

        @RepeatedTest(10)
        @DisplayName("id 로 VisitingHistory 엔티티 삭제")
        void deleteById() {
            // given
            User          user      = em.persistAndFlush(TestUtils.createUser());
            Station       station   = em.persistAndFlush(TestUtils.createStation());
            Place         place     = em.persistAndFlush(TestUtils.createPlace());
            LocalDateTime startTime = LocalDateTime.now();
            VisitingHistory visitingHistory = em.persistAndFlush(
                    TestUtils.createVisitingHistory(user, station, place, startTime)
            );
            Long id = visitingHistory.getId();

            // when
            visitingHistoryRepository.deleteById(id);
            em.flush();

            // then
            VisitingHistory deletedHistory = em.find(VisitingHistory.class, id);

            assertNull(deletedHistory, "deletedHistory 는 존재하지 않아야 합니다.");
        }

    }

    @Nested
    @DisplayName("VisitingHistory 엔티티 비즈니스 메서드 테스트")
    class BusinessMethodTests {

        @RepeatedTest(10)
        @DisplayName("endJourney() - 여정 종료 (성공)")
        void endJourney_success() {
            // given
            User            user            = em.persistAndFlush(TestUtils.createUser());
            Station         station         = em.persistAndFlush(TestUtils.createStation());
            Place           place           = em.persistAndFlush(TestUtils.createPlace());
            LocalDateTime   startTime       = LocalDateTime.now();
            LocalDateTime   departureTime   = startTime.plusMinutes(30);
            LocalDateTime   endTime         = startTime.plusMinutes(20);
            VisitingHistory visitingHistory = VisitingHistory.of(user, station, place, startTime, departureTime);
            em.persistAndFlush(visitingHistory);
            Long id = visitingHistory.getId();

            // when
            visitingHistory.endJourney(endTime);
            em.flush();

            // then
            VisitingHistory updatedHistory = em.find(VisitingHistory.class, id);

            assertNotNull(updatedHistory, "updatedHistory 는 null 이 아니어야 합니다.");
            assertEquals(endTime, updatedHistory.getEndTime(), "endTime 은 종료 시간과 같아야 합니다.");
            assertTrue(updatedHistory.isSuccess(), "isSuccess 는 true 여야 합니다 (성공).");
            assertNotNull(updatedHistory.getTotalDurationMinutes(), "totalDurationMinutes 는 null 이 아니어야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("endJourney() - 여정 종료 (실패 - 지연)")
        void endJourney_fail() {
            // given
            User            user            = em.persistAndFlush(TestUtils.createUser());
            Station         station         = em.persistAndFlush(TestUtils.createStation());
            Place           place           = em.persistAndFlush(TestUtils.createPlace());
            LocalDateTime   startTime       = LocalDateTime.now();
            LocalDateTime   departureTime   = startTime.plusMinutes(30);
            LocalDateTime   endTime         = startTime.plusMinutes(35);
            VisitingHistory visitingHistory = VisitingHistory.of(user, station, place, startTime, departureTime);
            em.persistAndFlush(visitingHistory);
            Long id = visitingHistory.getId();

            // when
            visitingHistory.endJourney(endTime);
            em.flush();

            // then
            VisitingHistory updatedHistory = em.find(VisitingHistory.class, id);

            assertNotNull(updatedHistory, "updatedHistory 는 null 이 아니어야 합니다.");
            assertEquals(endTime, updatedHistory.getEndTime(), "endTime 은 종료 시간과 같아야 합니다.");
            assertFalse(updatedHistory.isSuccess(), "isSuccess 는 false 여야 합니다 (지연).");
        }

        @RepeatedTest(10)
        @DisplayName("abandonJourney() - 여정 포기")
        void abandonJourney() {
            // given
            User          user          = em.persistAndFlush(TestUtils.createUser());
            Station       station       = em.persistAndFlush(TestUtils.createStation());
            Place         place         = em.persistAndFlush(TestUtils.createPlace());
            LocalDateTime startTime     = LocalDateTime.now();
            LocalDateTime departureTime = startTime.plusMinutes(30);
            LocalDateTime endTime       = startTime.plusMinutes(10);
            VisitingHistory visitingHistory = VisitingHistory.of(
                    user, station, place, startTime, endTime, departureTime
            );
            em.persistAndFlush(visitingHistory);
            Long id = visitingHistory.getId();

            // when
            visitingHistory.abandonJourney();
            em.flush();

            // then
            VisitingHistory updatedHistory = em.find(VisitingHistory.class, id);

            assertNotNull(updatedHistory, "updatedHistory 는 null 이 아니어야 합니다.");
            assertNull(updatedHistory.getEndTime(), "endTime 은 null 이어야 합니다 (포기).");
            assertFalse(updatedHistory.isSuccess(), "isSuccess 는 false 여야 합니다 (포기).");
        }

        @RepeatedTest(10)
        @DisplayName("isInProgress() - 진행 중 여부 확인")
        void isInProgress() {
            // given
            User            user            = em.persistAndFlush(TestUtils.createUser());
            Station         station         = em.persistAndFlush(TestUtils.createStation());
            Place           place           = em.persistAndFlush(TestUtils.createPlace());
            LocalDateTime   startTime       = LocalDateTime.now();
            LocalDateTime   departureTime   = startTime.plusMinutes(30);
            VisitingHistory visitingHistory = VisitingHistory.of(user, station, place, startTime, departureTime);
            em.persistAndFlush(visitingHistory);

            // when & then
            assertTrue(visitingHistory.isInProgress(), "진행 중이면 true 여야 합니다.");

            // when
            visitingHistory.endJourney(startTime.plusMinutes(20));
            em.flush();

            // then
            assertFalse(visitingHistory.isInProgress(), "종료되었으면 false 여야 합니다.");
        }

    }

    @Nested
    @DisplayName("findVisitingHistoryList() 테스트")
    class FindVisitingHistoryListTests {

        @RepeatedTest(10)
        @DisplayName("userId 로 방문 이력 목록 조회")
        void findVisitingHistoryList() {
            // given
            User user = em.persistAndFlush(TestUtils.createUser());
            List<Station> stations = TestUtils.createStations(5)
                                              .stream()
                                              .map(em::persistAndFlush)
                                              .toList();
            Place         place     = em.persistAndFlush(TestUtils.createPlace());
            LocalDateTime startTime = LocalDateTime.now();

            stations.stream()
                    .map(station -> em.persistAndFlush(
                            TestUtils.createVisitingHistory(user, station, place, startTime)
                    ))
                    .toList();

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

            // when
            Page<VisitingHistoryListResponse> responsePage = visitingHistoryRepository.findVisitingHistoryList(
                    user.getId(), null, pageable
            );

            // then
            assertNotNull(responsePage, "responsePage 는 null 이 아니어야 합니다.");
            assertEquals(5, responsePage.getTotalElements(), "총 요소 개수는 5 여야 합니다.");
            assertEquals(5, responsePage.getContent().size(), "콘텐츠 크기는 5 여야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("userId 와 keyword 로 방문 이력 목록 조회 (역 이름 검색)")
        void findVisitingHistoryList_withStationKeyword() {
            // given
            User          user      = em.persistAndFlush(TestUtils.createUser());
            Station       station1  = em.persistAndFlush(TestUtils.createStation("서울역"));
            Station       station2  = em.persistAndFlush(TestUtils.createStation("부산역"));
            Station       station3  = em.persistAndFlush(TestUtils.createStation("대구역"));
            Place         place     = em.persistAndFlush(TestUtils.createPlace());
            LocalDateTime startTime = LocalDateTime.now();

            em.persistAndFlush(TestUtils.createVisitingHistory(user, station1, place, startTime));
            em.persistAndFlush(TestUtils.createVisitingHistory(user, station2, place, startTime));
            em.persistAndFlush(TestUtils.createVisitingHistory(user, station3, place, startTime));

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

            // when
            Page<VisitingHistoryListResponse> responsePage = visitingHistoryRepository.findVisitingHistoryList(
                    user.getId(), "서울", pageable
            );

            // then
            assertNotNull(responsePage, "responsePage 는 null 이 아니어야 합니다.");
            assertEquals(1, responsePage.getTotalElements(), "총 요소 개수는 1 여야 합니다.");
            assertEquals(1, responsePage.getContent().size(), "콘텐츠 크기는 1 이어야 합니다.");
            assertEquals("서울역", responsePage.getContent().get(0).getStationName(), "역 이름은 '서울역'이어야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("userId 와 keyword 로 방문 이력 목록 조회 (장소 이름 검색)")
        void findVisitingHistoryList_withPlaceKeyword() {
            // given
            User          user      = em.persistAndFlush(TestUtils.createUser());
            Station       station   = em.persistAndFlush(TestUtils.createStation("서울역"));
            Place         place1    = em.persistAndFlush(TestUtils.createPlace("스타벅스"));
            Place         place2    = em.persistAndFlush(TestUtils.createPlace("이디야"));
            Place         place3    = em.persistAndFlush(TestUtils.createPlace("할리스"));
            LocalDateTime startTime = LocalDateTime.now();

            em.persistAndFlush(TestUtils.createVisitingHistory(user, station, place1, startTime));
            em.persistAndFlush(TestUtils.createVisitingHistory(user, station, place2, startTime));
            em.persistAndFlush(TestUtils.createVisitingHistory(user, station, place3, startTime));

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

            // when
            Page<VisitingHistoryListResponse> responsePage = visitingHistoryRepository.findVisitingHistoryList(
                    user.getId(), "스타벅스", pageable
            );

            // then
            assertNotNull(responsePage, "responsePage 는 null 이 아니어야 합니다.");
            assertEquals(1, responsePage.getTotalElements(), "총 요소 개수는 1 여야 합니다.");
            assertEquals(1, responsePage.getContent().size(), "콘텐츠 크기는 1 이어야 합니다.");
            assertEquals("스타벅스", responsePage.getContent().get(0).getPlaceName(), "장소 이름은 '스타벅스'이어야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("userId 와 keyword 로 방문 이력 목록 조회 (대소문자 무시)")
        void findVisitingHistoryList_withKeyword_caseInsensitive() {
            // given
            User          user      = em.persistAndFlush(TestUtils.createUser());
            Station       station1  = em.persistAndFlush(TestUtils.createStation("Seoul Station"));
            Station       station2  = em.persistAndFlush(TestUtils.createStation("Busan Station"));
            Place         place     = em.persistAndFlush(TestUtils.createPlace());
            LocalDateTime startTime = LocalDateTime.now();

            em.persistAndFlush(TestUtils.createVisitingHistory(user, station1, place, startTime));
            em.persistAndFlush(TestUtils.createVisitingHistory(user, station2, place, startTime));

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

            // when
            Page<VisitingHistoryListResponse> responsePage = visitingHistoryRepository.findVisitingHistoryList(
                    user.getId(), "seoul", pageable
            );

            // then
            assertNotNull(responsePage, "responsePage 는 null 이 아니어야 합니다.");
            assertEquals(1, responsePage.getTotalElements(), "총 요소 개수는 1 여야 합니다.");
            assertEquals(1, responsePage.getContent().size(), "콘텐츠 크기는 1 이어야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("userId 로 방문 이력 목록 조회 - 빈 결과")
        void findVisitingHistoryList_emptyResult() {
            // given
            User user = em.persistAndFlush(TestUtils.createUser());

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<VisitingHistoryListResponse> responsePage = visitingHistoryRepository.findVisitingHistoryList(
                    user.getId(), null, pageable
            );

            // then
            assertNotNull(responsePage, "responsePage 는 null 이 아니어야 합니다.");
            assertEquals(0, responsePage.getTotalElements(), "총 요소 개수는 0 이어야 합니다.");
            assertTrue(responsePage.isEmpty(), "결과가 비어있어야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("방문 이력 목록 조회 - 정렬 (createdAt 기준 내림차순)")
        void findVisitingHistoryList_sortByCreatedAt() {
            // given
            User    user     = em.persistAndFlush(TestUtils.createUser());
            Station station1 = em.persistAndFlush(TestUtils.createStation("역 1"));
            Station station2 = em.persistAndFlush(TestUtils.createStation("역 2"));
            Station station3 = em.persistAndFlush(TestUtils.createStation("역 3"));
            Place   place    = em.persistAndFlush(TestUtils.createPlace());

            LocalDateTime time1 = LocalDateTime.now().minusHours(3);
            LocalDateTime time2 = LocalDateTime.now().minusHours(2);
            LocalDateTime time3 = LocalDateTime.now().minusHours(1);

            em.persistAndFlush(TestUtils.createVisitingHistory(user, station1, place, time1));
            em.persistAndFlush(TestUtils.createVisitingHistory(user, station2, place, time2));
            em.persistAndFlush(TestUtils.createVisitingHistory(user, station3, place, time3));

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

            // when
            Page<VisitingHistoryListResponse> responsePage = visitingHistoryRepository.findVisitingHistoryList(
                    user.getId(), null, pageable
            );

            // then
            assertNotNull(responsePage, "responsePage 는 null 이 아니어야 합니다.");
            assertEquals(3, responsePage.getTotalElements(), "총 요소 개수는 3 이어야 합니다.");
            assertEquals("역 3", responsePage.getContent().get(0).getStationName(), "첫 번째 요소는 가장 최근 생성된 것이어야 합니다.");
            assertEquals("역 1", responsePage.getContent().get(2).getStationName(), "세 번째 요소는 가장 이전에 생성된 것이어야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("방문 이력 목록 조회 - 페이지네이션")
        void findVisitingHistoryList_pagination() {
            // given
            User user = em.persistAndFlush(TestUtils.createUser());
            List<Station> stations = TestUtils.createStations(15)
                                              .stream()
                                              .map(em::persistAndFlush)
                                              .toList();
            Place         place     = em.persistAndFlush(TestUtils.createPlace());
            LocalDateTime startTime = LocalDateTime.now();

            stations.stream()
                    .map(station -> em.persistAndFlush(
                            TestUtils.createVisitingHistory(user, station, place, startTime)
                    ))
                    .toList();

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<VisitingHistoryListResponse> responsePage = visitingHistoryRepository.findVisitingHistoryList(
                    user.getId(), null, pageable
            );

            // then
            assertNotNull(responsePage, "responsePage 는 null 이 아니어야 합니다.");
            assertEquals(15, responsePage.getTotalElements(), "총 요소 개수는 15 여야 합니다.");
            assertEquals(10, responsePage.getContent().size(), "첫 페이지의 콘텐츠 크기는 10 이어야 합니다.");
            assertEquals(2, responsePage.getTotalPages(), "총 페이지 수는 2 여야 합니다.");
            assertTrue(responsePage.hasNext(), "다음 페이지가 존재해야 합니다.");
        }

    }

    @Nested
    @DisplayName("User 통계 업데이트 테스트")
    class UserStatisticsTests {

        @RepeatedTest(10)
        @DisplayName("addVisitHistory() - 방문 이력 추가 시 사용자 통계 업데이트")
        void addVisitHistory() {
            // given
            User            user            = em.persistAndFlush(TestUtils.createUser());
            Station         station         = em.persistAndFlush(TestUtils.createStation());
            Place           place           = em.persistAndFlush(TestUtils.createPlace());
            LocalDateTime   startTime       = LocalDateTime.now();
            VisitingHistory visitingHistory = TestUtils.createVisitingHistory(user, station, place, startTime);

            int beforeVisitCount     = user.getTotalVisitCount();
            int beforeJourneyMinutes = user.getTotalJourneyMinutes();

            // when
            user.addVisitHistory(20, true); // 20 분, 성공
            em.flush();

            // then
            User updatedUser = em.find(User.class, user.getId());

            assertNotNull(updatedUser, "updatedUser 는 null 이 아니어야 합니다.");
            assertEquals(beforeVisitCount + 1, updatedUser.getTotalVisitCount(),
                         "totalVisitCount 는 1 증가해야 합니다.");
            assertEquals(beforeJourneyMinutes + 20, updatedUser.getTotalJourneyMinutes(),
                         "totalJourneyMinutes 는 20 분 증가해야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("removeVisitHistory() - 방문 이력 제거 시 사용자 통계 업데이트")
        void removeVisitHistory() {
            // given
            User            user            = em.persistAndFlush(TestUtils.createUser());
            Station         station         = em.persistAndFlush(TestUtils.createStation());
            Place           place           = em.persistAndFlush(TestUtils.createPlace());
            LocalDateTime   startTime       = LocalDateTime.now();
            VisitingHistory visitingHistory = TestUtils.createVisitingHistory(user, station, place, startTime);
            em.persistAndFlush(visitingHistory);

            // 먼저 방문 이력 추가
            user.addVisitHistory(20, true);
            em.flush();

            int beforeVisitCount     = user.getTotalVisitCount();
            int beforeJourneyMinutes = user.getTotalJourneyMinutes();

            // when
            user.removeVisitHistory(20, true); // 20 분, 성공
            em.flush();

            // then
            User updatedUser = em.find(User.class, user.getId());

            assertNotNull(updatedUser, "updatedUser 는 null 이 아니어야 합니다.");
            assertEquals(beforeVisitCount - 1, updatedUser.getTotalVisitCount(),
                         "totalVisitCount 는 1 감소해야 합니다.");
            assertEquals(beforeJourneyMinutes - 20, updatedUser.getTotalJourneyMinutes(),
                         "totalJourneyMinutes 는 20 분 감소해야 합니다.");
        }

    }

}
