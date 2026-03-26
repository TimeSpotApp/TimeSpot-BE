package com.timespot.backend.domain.favorite.dao;

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
import com.timespot.backend.domain.favorite.dto.FavoriteResponseDto.FavoriteListResponse;
import com.timespot.backend.domain.favorite.model.Favorite;
import com.timespot.backend.domain.place.model.Station;
import com.timespot.backend.domain.user.model.User;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
 * PackageName : com.timespot.backend.domain.favorite.dao
 * FileName    : FavoriteRepositoryTest
 * Author      : loadingKKamo21
 * Date        : 26. 3. 25.
 * Description : 즐겨찾기 리포지토리 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 25.    loadingKKamo21       Initial creation
 */
@DataJpaTest
@Import({EnableJpaAuditingConfig.class, P6SpyConfig.class, QuerydslConfig.class})
class FavoriteRepositoryTest {

    @Autowired
    private TestEntityManager  em;
    @Autowired
    private FavoriteRepository favoriteRepository;

    @Nested
    @DisplayName("save() 테스트")
    class SaveTests {

        @RepeatedTest(10)
        @DisplayName("Favorite 엔티티 저장")
        void save() {
            // given
            User     user     = em.persistAndFlush(TestUtils.createUser());
            Station  station  = em.persistAndFlush(TestUtils.createStation());
            Favorite favorite = TestUtils.createFavorite(user, station);

            // when
            Long id = favoriteRepository.save(favorite).getId();
            em.flush();

            // then
            Favorite savedFavorite = em.find(Favorite.class, id);

            assertNotNull(savedFavorite, "savedFavorite 는 null 이 아니어야 합니다.");
            assertEquals(favorite.getUser(), savedFavorite.getUser(), "user 는 같아야 합니다.");
            assertEquals(favorite.getStation(), savedFavorite.getStation(), "station 은 같아야 합니다.");
            assertEquals(favorite.getVisitCount(), savedFavorite.getVisitCount(), "visitCount 는 같아야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("Favorite 엔티티 저장 시 visitCount 기본값 0")
        void save_defaultVisitCount() {
            // given
            User     user     = em.persistAndFlush(TestUtils.createUser());
            Station  station  = em.persistAndFlush(TestUtils.createStation());
            Favorite favorite = Favorite.of(user, station);

            // when
            Long id = favoriteRepository.save(favorite).getId();
            em.flush();

            // then
            Favorite savedFavorite = em.find(Favorite.class, id);

            assertNotNull(savedFavorite, "savedFavorite 는 null 이 아니어야 합니다.");
            assertEquals(0, savedFavorite.getVisitCount(), "visitCount 의 기본값은 0 이어야 합니다.");
        }

    }

    @Nested
    @DisplayName("findById() 테스트")
    class FindByIdTests {

        @RepeatedTest(10)
        @DisplayName("id 로 Favorite 엔티티 단 건 조회")
        void findById() {
            // given
            User     user     = em.persistAndFlush(TestUtils.createUser());
            Station  station  = em.persistAndFlush(TestUtils.createStation());
            Favorite favorite = em.persistAndFlush(TestUtils.createFavorite(user, station));
            Long     id       = favorite.getId();

            // when
            Favorite findFavorite = favoriteRepository.findById(id).get();

            // then
            assertNotNull(findFavorite, "findFavorite 는 null 이 아니어야 합니다.");
            assertEquals(favorite.getUser(), findFavorite.getUser(), "user 는 같아야 합니다.");
            assertEquals(favorite.getStation(), findFavorite.getStation(), "station 은 같아야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("존재하지 않는 id 로 Favorite 엔티티 단 건 조회 시도")
        void findById_unknownId(@Min(1) final long unknownId) {
            // when
            Optional<Favorite> opFavorite = favoriteRepository.findById(unknownId);

            // then
            assertFalse(opFavorite.isPresent(), "조회된 Favorite 엔티티가 존재하지 않아야 합니다.");
        }

    }

    @Nested
    @DisplayName("existsByUserIdAndStationId() 테스트")
    class ExistsByUserIdAndStationIdTests {

        @RepeatedTest(10)
        @DisplayName("userId, stationId 로 Favorite 엔티티 존재 여부 확인")
        void existsByUserIdAndStationId() {
            // given
            User    user    = em.persistAndFlush(TestUtils.createUser());
            Station station = em.persistAndFlush(TestUtils.createStation());
            em.persistAndFlush(TestUtils.createFavorite(user, station));
            UUID userId    = user.getId();
            Long stationId = station.getId();

            // when
            boolean exists = favoriteRepository.existsByUserIdAndStationId(userId, stationId);

            // then
            assertTrue(exists, "Favorite 엔티티가 존재해야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("존재하지 않는 userId, stationId 로 Favorite 엔티티 존재 여부 확인")
        void existsByUserIdAndStationId_notExists(final UUID unknownUserId, @Min(1) final long unknownStationId) {
            // when
            boolean exists = favoriteRepository.existsByUserIdAndStationId(unknownUserId, unknownStationId);

            // then
            assertFalse(exists, "Favorite 엔티티가 존재하지 않아야 합니다.");
        }

    }

    @Nested
    @DisplayName("deleteById() 테스트")
    class DeleteByIdTests {

        @RepeatedTest(10)
        @DisplayName("id 로 Favorite 엔티티 삭제")
        void deleteById() {
            // given
            User     user     = em.persistAndFlush(TestUtils.createUser());
            Station  station  = em.persistAndFlush(TestUtils.createStation());
            Favorite favorite = em.persistAndFlush(TestUtils.createFavorite(user, station));
            Long     id       = favorite.getId();

            // when
            favoriteRepository.deleteById(id);
            em.flush();

            // then
            Favorite deletedFavorite = em.find(Favorite.class, id);

            assertNull(deletedFavorite, "deletedFavorite 는 존재하지 않아야 합니다.");
        }

    }

    @Nested
    @DisplayName("findFavoriteStationList() 테스트")
    class FindFavoriteStationListTests {

        @RepeatedTest(10)
        @DisplayName("userId 로 즐겨찾기 역 목록 조회")
        void findFavoriteStationList() {
            // given
            User user = em.persistAndFlush(TestUtils.createUser());
            List<Station> stations = TestUtils.createStations(5)
                                              .stream()
                                              .map(em::persistAndFlush)
                                              .toList();
            stations.stream()
                    .map(station -> em.persistAndFlush(TestUtils.createFavorite(user, station)))
                    .toList();

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

            // when
            Page<FavoriteListResponse> responsePage = favoriteRepository.findFavoriteStationList(
                    user.getId(), null, pageable
            );

            // then
            assertNotNull(responsePage, "responsePage 는 null 이 아니어야 합니다.");
            assertEquals(5, responsePage.getTotalElements(), "총 요소 개수는 5 여야 합니다.");
            assertEquals(5, responsePage.getContent().size(), "콘텐츠 크기는 5 여야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("userId 와 keyword 로 즐겨찾기 역 목록 조회 (이름 포함 검색)")
        void findFavoriteStationList_withKeyword() {
            // given
            User    user     = em.persistAndFlush(TestUtils.createUser());
            Station station1 = em.persistAndFlush(TestUtils.createStation("서울역"));
            Station station2 = em.persistAndFlush(TestUtils.createStation("부산역"));
            Station station3 = em.persistAndFlush(TestUtils.createStation("대구역"));
            em.persistAndFlush(TestUtils.createFavorite(user, station1));
            em.persistAndFlush(TestUtils.createFavorite(user, station2));
            em.persistAndFlush(TestUtils.createFavorite(user, station3));

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

            // when
            Page<FavoriteListResponse> responsePage = favoriteRepository.findFavoriteStationList(
                    user.getId(), "서울", pageable
            );

            // then
            assertNotNull(responsePage, "responsePage 는 null 이 아니어야 합니다.");
            assertEquals(1, responsePage.getTotalElements(), "총 요소 개수는 1 여야 합니다.");
            assertEquals(1, responsePage.getContent().size(), "콘텐츠 크기는 1 이어야 합니다.");
            assertEquals("서울역", responsePage.getContent().get(0).getStationName(), "역 이름은 '서울역'이어야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("userId 와 keyword 로 즐겨찾기 역 목록 조회 (대소문자 무시)")
        void findFavoriteStationList_withKeyword_caseInsensitive() {
            // given
            User    user     = em.persistAndFlush(TestUtils.createUser());
            Station station1 = em.persistAndFlush(TestUtils.createStation("Seoul Station"));
            Station station2 = em.persistAndFlush(TestUtils.createStation("Busan Station"));
            em.persistAndFlush(TestUtils.createFavorite(user, station1));
            em.persistAndFlush(TestUtils.createFavorite(user, station2));

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

            // when
            Page<FavoriteListResponse> responsePage = favoriteRepository.findFavoriteStationList(
                    user.getId(), "seoul", pageable
            );

            // then
            assertNotNull(responsePage, "responsePage 는 null 이 아니어야 합니다.");
            assertEquals(1, responsePage.getTotalElements(), "총 요소 개수는 1 여야 합니다.");
            assertEquals(1, responsePage.getContent().size(), "콘텐츠 크기는 1 이어야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("userId 로 즐겨찾기 역 목록 조회 - 빈 결과")
        void findFavoriteStationList_emptyResult() {
            // given
            User user = em.persistAndFlush(TestUtils.createUser());

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<FavoriteListResponse> responsePage = favoriteRepository.findFavoriteStationList(
                    user.getId(), null, pageable
            );

            // then
            assertNotNull(responsePage, "responsePage 는 null 이 아니어야 합니다.");
            assertEquals(0, responsePage.getTotalElements(), "총 요소 개수는 0 이어야 합니다.");
            assertTrue(responsePage.isEmpty(), "결과가 비어있어야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("즐겨찾기 역 목록 조회 - 정렬 (visitCount 기준 내림차순)")
        void findFavoriteStationList_sortByVisitCount() {
            // given
            User    user     = em.persistAndFlush(TestUtils.createUser());
            Station station1 = em.persistAndFlush(TestUtils.createStation("역 1"));
            Station station2 = em.persistAndFlush(TestUtils.createStation("역 2"));
            Station station3 = em.persistAndFlush(TestUtils.createStation("역 3"));

            Favorite fav1 = em.persistAndFlush(TestUtils.createFavorite(user, station1));
            Favorite fav2 = em.persistAndFlush(TestUtils.createFavorite(user, station2));
            em.persistAndFlush(TestUtils.createFavorite(user, station3));

            fav1.incrementVisitCount();
            fav1.incrementVisitCount();
            fav1.incrementVisitCount();
            fav2.incrementVisitCount();
            fav2.incrementVisitCount();
            em.flush();

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "visitCount"));

            // when
            Page<FavoriteListResponse> responsePage = favoriteRepository.findFavoriteStationList(
                    user.getId(), null, pageable
            );

            // then
            assertNotNull(responsePage, "responsePage 는 null 이 아니어야 합니다.");
            assertEquals(3, responsePage.getTotalElements(), "총 요소 개수는 3 이어야 합니다.");
            assertEquals(3, responsePage.getContent().get(0).getVisitCount(), "첫 번째 요소의 visitCount 는 3 이어야 합니다.");
            assertEquals(2, responsePage.getContent().get(1).getVisitCount(), "두 번째 요소의 visitCount 는 2 여야 합니다.");
            assertEquals(0, responsePage.getContent().get(2).getVisitCount(), "세 번째 요소의 visitCount 는 0 이어야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("즐겨찾기 역 목록 조회 - 페이지네이션")
        void findFavoriteStationList_pagination() {
            // given
            User user = em.persistAndFlush(TestUtils.createUser());
            List<Station> stations = TestUtils.createStations(15)
                                              .stream()
                                              .map(em::persistAndFlush)
                                              .toList();
            stations.stream()
                    .map(station -> em.persistAndFlush(TestUtils.createFavorite(user, station)))
                    .toList();

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<FavoriteListResponse> responsePage = favoriteRepository.findFavoriteStationList(
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
    @DisplayName("Favorite 엔티티 비즈니스 메서드 테스트")
    class BusinessMethodTests {

        @RepeatedTest(10)
        @DisplayName("incrementVisitCount() - 방문 횟수 증가")
        void incrementVisitCount() {
            // given
            User     user     = em.persistAndFlush(TestUtils.createUser());
            Station  station  = em.persistAndFlush(TestUtils.createStation());
            Favorite favorite = em.persistAndFlush(TestUtils.createFavorite(user, station));
            Long     id       = favorite.getId();

            // when
            favorite.incrementVisitCount();
            em.flush();

            // then
            Favorite updatedFavorite = em.find(Favorite.class, id);

            assertNotNull(updatedFavorite, "updatedFavorite 는 null 이 아니어야 합니다.");
            assertEquals(1, updatedFavorite.getVisitCount(), "visitCount 는 1 이어야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("decrementVisitCount() - 방문 횟수 감소")
        void decrementVisitCount() {
            // given
            User     user     = em.persistAndFlush(TestUtils.createUser());
            Station  station  = em.persistAndFlush(TestUtils.createStation());
            Favorite favorite = em.persistAndFlush(TestUtils.createFavorite(user, station));
            favorite.incrementVisitCount();
            favorite.incrementVisitCount();
            favorite.incrementVisitCount();
            em.flush();
            Long id = favorite.getId();

            // when
            favorite.decrementVisitCount();
            em.flush();

            // then
            Favorite updatedFavorite = em.find(Favorite.class, id);

            assertNotNull(updatedFavorite, "updatedFavorite 는 null 이 아니어야 합니다.");
            assertEquals(2, updatedFavorite.getVisitCount(), "visitCount 는 2 여야 합니다.");
        }

    }

}
