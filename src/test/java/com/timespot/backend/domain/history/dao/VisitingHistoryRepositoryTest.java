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
import com.timespot.backend.domain.history.model.VisitingHistory;
import com.timespot.backend.domain.history.model.VisitingHistoryPlace;
import com.timespot.backend.domain.place.model.Place;
import com.timespot.backend.domain.place.model.Station;
import com.timespot.backend.domain.user.model.User;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

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
            LocalDateTime   startTime       = LocalDateTime.now();
            VisitingHistory visitingHistory = TestUtils.createVisitingHistory(user, station, startTime);

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
            User          user      = em.persistAndFlush(TestUtils.createUser());
            Station       station   = em.persistAndFlush(TestUtils.createStation());
            LocalDateTime startTime = LocalDateTime.now();
            VisitingHistory visitingHistory = TestUtils.createVisitingHistory(
                    user, station, startTime
            );

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
            LocalDateTime startTime = LocalDateTime.now();
            VisitingHistory visitingHistory = em.persistAndFlush(
                    TestUtils.createVisitingHistory(user, station, startTime)
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
            LocalDateTime startTime = LocalDateTime.now();
            VisitingHistory visitingHistory = em.persistAndFlush(
                    TestUtils.createVisitingHistory(user, station, startTime)
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
    @DisplayName("VisitingHistoryPlace 엔티티 테스트")
    class VisitingHistoryPlaceTests {

        @RepeatedTest(10)
        @DisplayName("VisitingHistoryPlace 엔티티 저장")
        void saveVisitingHistoryPlace() {
            // given
            User          user      = em.persistAndFlush(TestUtils.createUser());
            Station       station   = em.persistAndFlush(TestUtils.createStation());
            Place         place     = em.persistAndFlush(TestUtils.createPlace());
            LocalDateTime startTime = LocalDateTime.now();
            VisitingHistory visitingHistory = em.persistAndFlush(
                    TestUtils.createVisitingHistory(user, station, startTime)
            );

            VisitingHistoryPlace visitingHistoryPlace = TestUtils.createVisitingHistoryPlace(
                    visitingHistory, place
            );

            // when
            Long id = em.persistAndFlush(visitingHistoryPlace).getId();
            em.flush();

            // then
            VisitingHistoryPlace savedPlace = em.find(VisitingHistoryPlace.class, id);

            assertNotNull(savedPlace, "savedPlace 는 null 이 아니어야 합니다.");
            assertEquals(visitingHistoryPlace.getVisitingHistory(), savedPlace.getVisitingHistory(),
                         "visitingHistory 는 같아야 합니다.");
            assertEquals(visitingHistoryPlace.getPlace(), savedPlace.getPlace(), "place 는 같아야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("VisitingHistoryPlace 엔티티 삭제")
        void deleteVisitingHistoryPlace() {
            // given
            User          user      = em.persistAndFlush(TestUtils.createUser());
            Station       station   = em.persistAndFlush(TestUtils.createStation());
            Place         place     = em.persistAndFlush(TestUtils.createPlace());
            LocalDateTime startTime = LocalDateTime.now();
            VisitingHistory visitingHistory = em.persistAndFlush(
                    TestUtils.createVisitingHistory(user, station, startTime)
            );
            VisitingHistoryPlace visitingHistoryPlace = em.persistAndFlush(
                    TestUtils.createVisitingHistoryPlace(visitingHistory, place)
            );
            Long id = visitingHistoryPlace.getId();

            // when
            em.remove(visitingHistoryPlace);
            em.flush();

            // then
            VisitingHistoryPlace deletedPlace = em.find(VisitingHistoryPlace.class, id);

            assertNull(deletedPlace, "deletedPlace 는 존재하지 않아야 합니다.");
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
            LocalDateTime   startTime       = LocalDateTime.now();
            LocalDateTime   departureTime   = startTime.plusMinutes(30);
            LocalDateTime   endTime         = startTime.plusMinutes(20);
            VisitingHistory visitingHistory = VisitingHistory.of(user, station, startTime, departureTime);
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
            LocalDateTime   startTime       = LocalDateTime.now();
            LocalDateTime   departureTime   = startTime.plusMinutes(30);
            LocalDateTime   endTime         = startTime.plusMinutes(35);
            VisitingHistory visitingHistory = VisitingHistory.of(user, station, startTime, departureTime);
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
            LocalDateTime startTime     = LocalDateTime.now();
            LocalDateTime departureTime = startTime.plusMinutes(30);
            LocalDateTime endTime       = startTime.plusMinutes(10);
            VisitingHistory visitingHistory = VisitingHistory.of(
                    user, station, startTime, endTime, departureTime
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
            LocalDateTime   startTime       = LocalDateTime.now();
            LocalDateTime   departureTime   = startTime.plusMinutes(30);
            VisitingHistory visitingHistory = VisitingHistory.of(user, station, startTime, departureTime);
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
    @DisplayName("User 통계 업데이트 테스트")
    class UserStatisticsTests {

        @RepeatedTest(10)
        @DisplayName("addVisitHistory() - 방문 이력 추가 시 사용자 통계 업데이트")
        void addVisitHistory() {
            // given
            User          user      = em.persistAndFlush(TestUtils.createUser());
            Station       station   = em.persistAndFlush(TestUtils.createStation());
            LocalDateTime startTime = LocalDateTime.now();
            VisitingHistory visitingHistory = TestUtils.createVisitingHistory(
                    user, station, startTime
            );

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
            User          user      = em.persistAndFlush(TestUtils.createUser());
            Station       station   = em.persistAndFlush(TestUtils.createStation());
            LocalDateTime startTime = LocalDateTime.now();
            VisitingHistory visitingHistory = TestUtils.createVisitingHistory(
                    user, station, startTime
            );
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
