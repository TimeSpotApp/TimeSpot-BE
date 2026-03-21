package com.timespot.backend.domain.user.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import autoparams.AutoSource;
import autoparams.Repeat;
import com.timespot.backend.common.config.EnableJpaAuditingConfig;
import com.timespot.backend.common.config.P6SpyConfig;
import com.timespot.backend.common.config.QuerydslConfig;
import com.timespot.backend.common.util.TestUtils;
import com.timespot.backend.domain.user.dto.UserResponseDto.UserInfoResponse;
import com.timespot.backend.domain.user.model.MapApi;
import com.timespot.backend.domain.user.model.SocialConnection;
import com.timespot.backend.domain.user.model.User;
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

/**
 * PackageName : com.timespot.backend.domain.user.dao
 * FileName    : UserRepositoryTest
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
@DataJpaTest
@Import({EnableJpaAuditingConfig.class, P6SpyConfig.class, QuerydslConfig.class})
class UserRepositoryTest {

    @Autowired
    private TestEntityManager em;
    @Autowired
    private UserRepository    userRepository;

    @Nested
    @DisplayName("save() 테스트")
    class SaveTests {

        @RepeatedTest(10)
        @DisplayName("User 엔티티 저장")
        void save() {
            // given
            User user = TestUtils.createUser();

            // when
            UUID id = userRepository.save(user).getId();
            em.flush();

            // then
            User savedUser = em.find(User.class, id);

            assertNotNull(savedUser, "savedUser는 null이 아니어야 합니다.");
            assertEquals(user.getEmail(), savedUser.getEmail(), "email은 같아야 합니다.");
            assertEquals(user.getNickname(), savedUser.getNickname(), "nickname은 같아야 합니다.");
            assertEquals(user.getMapApi(), savedUser.getMapApi(), "mapApi는 같아야 합니다.");
            assertEquals(user.getRole(), savedUser.getRole(), "role은 같아야 합니다.");
        }

    }

    @Nested
    @DisplayName("findById() 테스트")
    class FindByIdTests {

        @RepeatedTest(10)
        @DisplayName("id로 User 엔티티 단 건 조회")
        void findById() {
            // given
            User user = em.persistAndFlush(TestUtils.createUser());
            UUID id   = user.getId();

            // when
            User findUser = userRepository.findById(id).get();

            // then
            assertNotNull(findUser, "findUser는 null이 아니어야 합니다.");
            assertEquals(user.getEmail(), findUser.getEmail(), "email은 같아야 합니다.");
            assertEquals(user.getNickname(), findUser.getNickname(), "nickname은 같아야 합니다.");
            assertEquals(user.getMapApi(), findUser.getMapApi(), "mapApi는 같아야 합니다.");
            assertEquals(user.getRole(), findUser.getRole(), "role은 같아야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("존재하지 않는 id로 User 엔티티 단 건 조회 시도")
        void findById_unknownId(final UUID unknownId) {
            // when
            Optional<User> opUser = userRepository.findById(unknownId);

            // then
            assertFalse(opUser.isPresent(), "조회된 User 엔티티가 존재하지 않아야 합니다.");
        }

    }

    @Nested
    @DisplayName("findByEmail() 테스트")
    class FindByEmailTests {

        @RepeatedTest(10)
        @DisplayName("email로 User 엔티티 단 건 조회")
        void findByEmail() {
            // given
            User   user  = em.persistAndFlush(TestUtils.createUser());
            String email = user.getEmail();

            // when
            User findUser = userRepository.findByEmail(email).get();

            // then
            assertNotNull(findUser, "findUser는 null이 아니어야 합니다.");
            assertEquals(user.getEmail(), findUser.getEmail(), "email은 같아야 합니다.");
            assertEquals(user.getNickname(), findUser.getNickname(), "nickname은 같아야 합니다.");
            assertEquals(user.getMapApi(), findUser.getMapApi(), "mapApi는 같아야 합니다.");
            assertEquals(user.getRole(), findUser.getRole(), "role은 같아야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("존재하지 않는 email로 User 엔티티 단 건 조회 시도")
        void findByEmail_unknownEmail() {
            //give
            String unknownEmail = TestUtils.FAKER.internet().safeEmailAddress();

            // when
            Optional<User> opUser = userRepository.findByEmail(unknownEmail);

            // then
            assertFalse(opUser.isPresent(), "조회된 User 엔티티가 존재하지 않아야 합니다.");
        }

    }

    @Nested
    @DisplayName("existsByEmail() 테스트")
    class ExistsByEmailTests {

        @RepeatedTest(10)
        @DisplayName("email로 User 엔티티 존재 여부 확인")
        void existsByEmail() {
            // given
            User   user  = em.persistAndFlush(TestUtils.createUser());
            String email = user.getEmail();

            // when
            boolean exists = userRepository.existsByEmail(email);

            // then
            assertTrue(exists, "User 엔티티가 존재해야 합니다.");
        }

        @RepeatedTest(10)
        @DisplayName("존재하지 않는 email로 User 엔티티 존재 여부 확인")
        void existsByEmail_unknownEmail() {
            //give
            String unknownEmail = TestUtils.FAKER.internet().safeEmailAddress();

            // when
            boolean exists = userRepository.existsByEmail(unknownEmail);

            // then
            assertFalse(exists, "User 엔티티가 존재하지 않아야 합니다.");
        }

    }

    @Nested
    @DisplayName("User 엔티티 필드 업데이트 테스트")
    class DirtyCheckingTests {

        @RepeatedTest(10)
        @DisplayName("User 엔티티 필드 업데이트")
        void update() {
            // given
            User   user           = em.persistAndFlush(TestUtils.createUser());
            UUID   id             = user.getId();
            String beforeNickname = user.getNickname();
            MapApi beforeMapApi   = user.getMapApi();

            // when
            User findUser = userRepository.findById(id).get();
            findUser.updateNickname(String.format("updated%s", findUser.getNickname()));
            findUser.updateMapApi(MapApi.GOOGLE);
            em.flush();

            // then
            User updatedUser = em.find(User.class, id);

            assertNotNull(updatedUser, "updatedUser는 존재하지 않아야 합니다.");
            assertEquals(findUser.getNickname(), updatedUser.getNickname(), "nickname은 업데이트된 값과 같아야 합니다.");
            assertNotEquals(beforeNickname, updatedUser.getNickname(), "nickname은 업데이트 이전 값과 달라야 합니다.");
            assertEquals(findUser.getMapApi(), updatedUser.getMapApi(), "mapApi는 업데이트된 값과 같아야 합니다.");
            assertNotEquals(beforeMapApi, updatedUser.getMapApi(), "mapApi는 업데이트 이전 값과 달라야 합니다.");
        }

    }

    @Nested
    @DisplayName("deleteById() 테스트")
    class DeleteByIdTests {

        @RepeatedTest(10)
        @DisplayName("id로 User 엔티티 삭제")
        void deleteById() {
            // given
            User user = em.persistAndFlush(TestUtils.createUser());
            UUID id   = user.getId();

            // when
            userRepository.deleteById(id);
            em.flush();

            // then
            User deletedUser = em.find(User.class, id);

            assertNull(deletedUser, "deletedUser는 존재하지 않아야 합니다.");
        }

    }

    @Nested
    @DisplayName("findByUserInfoById() 테스트")
    class FindByUserInfoByIdTests {

        @RepeatedTest(10)
        @DisplayName("id로 User DTO 단 건 조회")
        void findByUserInfoById() {
            // given
            User             user             = em.persistAndFlush(TestUtils.createUser());
            SocialConnection socialConnection = em.persistAndFlush(TestUtils.createSocialConnection(user));
            UUID             id               = user.getId();

            // when
            UserInfoResponse userInfoResponse = userRepository.findUserInfoById(id).get();

            // then
            assertNotNull(userInfoResponse, "userInfoResponse는 null이 아니어야 합니다.");
            assertEquals(user.getEmail(), userInfoResponse.getEmail(), "email은 같아야 합니다.");
            assertEquals(user.getNickname(), userInfoResponse.getNickname(), "nickname은 같아야 합니다.");
            assertEquals(user.getRole().name(), userInfoResponse.getRole(), "role은 같아야 합니다.");
            assertEquals(user.getMapApi().name(), userInfoResponse.getMapApi(), "mapApi는 같아야 합니다.");
            assertEquals(socialConnection.getProviderType().name(), userInfoResponse.getProviderType(),
                         "providerType은 같아야 합니다.");
            assertEquals(user.getCreatedAt().withNano(0), userInfoResponse.getCreatedAt().withNano(0),
                         "createdAt은 같아야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("존재하지 않는 id로 User DTO 단 건 조회 시도")
        void findByUserInfoById_unknownId(final UUID unknownId) {
            // when
            Optional<UserInfoResponse> opUserInfoResponse = userRepository.findUserInfoById(unknownId);

            // then
            assertFalse(opUserInfoResponse.isPresent(), "조회된 User DTO가 존재하지 않아야 합니다.");
        }

    }

}