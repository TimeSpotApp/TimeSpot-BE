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
import com.timespot.backend.domain.user.model.ProviderType;
import com.timespot.backend.domain.user.model.SocialConnection;
import com.timespot.backend.domain.user.model.User;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
 * FileName    : SocialConnectionRepositoryTest
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
class SocialConnectionRepositoryTest {

    @Autowired
    private TestEntityManager          em;
    @Autowired
    private SocialConnectionRepository socialConnectionRepository;

    @Nested
    @DisplayName("save() 테스트")
    class SaveTests {

        @RepeatedTest(10)
        @DisplayName("SocialConnection 엔티티 저장")
        void save() {
            // given
            User             user             = em.persistAndFlush(TestUtils.createUser());
            SocialConnection socialConnection = TestUtils.createSocialConnection(user);

            // when
            Long id = socialConnectionRepository.save(socialConnection).getId();
            em.flush();

            // then
            SocialConnection savedSocialConnection = em.find(SocialConnection.class, id);

            assertNotNull(savedSocialConnection, "savedSocialConnection는 null이 아니어야 합니다.");
            assertEquals(socialConnection.getUser(), savedSocialConnection.getUser(), "user는 같아야 합니다.");
            assertEquals(socialConnection.getProviderType(), savedSocialConnection.getProviderType(),
                         "providerType는 같아야 합니다.");
            assertEquals(socialConnection.getProviderId(), savedSocialConnection.getProviderId(),
                         "providerId는 같아야 합니다.");
        }

    }

    @Nested
    @DisplayName("findById() 테스트")
    class FindByIdTests {

        @RepeatedTest(10)
        @DisplayName("id로 SocialConnection 엔티티 단 건 조회")
        void findById() {
            // given
            User             user             = em.persistAndFlush(TestUtils.createUser());
            SocialConnection socialConnection = em.persistAndFlush(TestUtils.createSocialConnection(user));
            Long             id               = socialConnection.getId();

            // when
            SocialConnection findSocialConnection = socialConnectionRepository.findById(id).get();

            // then
            assertNotNull(findSocialConnection, "findSocialConnection는 null이 아니어야 합니다.");
            assertEquals(socialConnection.getUser(), findSocialConnection.getUser(), "user는 같아야 합니다.");
            assertEquals(socialConnection.getProviderType(), findSocialConnection.getProviderType(),
                         "providerType는 같아야 합니다.");
            assertEquals(socialConnection.getProviderId(), findSocialConnection.getProviderId(),
                         "providerId는 같아야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("존재하지 않는 id로 SocialConnection 엔티티 단 건 조회 시도")
        void findById_unknownId(@Min(1) @Max(Long.MAX_VALUE) final long unknownId) {
            // when
            Optional<SocialConnection> opSocialConnection = socialConnectionRepository.findById(unknownId);

            // then
            assertFalse(opSocialConnection.isPresent(), "조회된 SocialConnection 엔티티가 존재하지 않아야 합니다.");
        }

    }

    @Nested
    @DisplayName("findByProviderTypeAndProviderId() 테스트")
    class FindByProviderTypeAndProviderIdTests {

        @RepeatedTest(10)
        @DisplayName("providerType, providerId로 SocialConnection 엔티티 단 건 조회")
        void findByProviderTypeAndProviderId() {
            // given
            User             user             = em.persistAndFlush(TestUtils.createUser());
            SocialConnection socialConnection = em.persistAndFlush(TestUtils.createSocialConnection(user));
            ProviderType     providerType     = socialConnection.getProviderType();
            String           providerId       = socialConnection.getProviderId();

            // when
            SocialConnection findSocialConnection = socialConnectionRepository.findByProviderTypeAndProviderId(
                    providerType, providerId
            ).get();

            // then
            assertNotNull(findSocialConnection, "findSocialConnection는 null이 아니어야 합니다.");
            assertEquals(socialConnection.getUser(), findSocialConnection.getUser(), "user는 같아야 합니다.");
            assertEquals(socialConnection.getProviderType(), findSocialConnection.getProviderType(),
                         "providerType는 같아야 합니다.");
            assertEquals(socialConnection.getProviderId(), findSocialConnection.getProviderId(),
                         "providerId는 같아야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("providerType, 존재하지 않는 providerId로 SocialConnection 엔티티 단 건 조회 시도")
        void findByProviderTypeAndProviderId_unknownProviderId(@NotNull final ProviderType providerType,
                                                               @NotBlank final String unknownProviderId) {
            // when
            Optional<SocialConnection> opSocialConnection = socialConnectionRepository.findByProviderTypeAndProviderId(
                    providerType, unknownProviderId
            );

            // then
            assertFalse(opSocialConnection.isPresent(), "조회된 SocialConnection 엔티티가 존재하지 않아야 합니다.");
        }

    }

    @Nested
    @DisplayName("existsByProviderTypeAndProviderId() 테스트")
    class ExistsByProviderTypeAndProviderIdTests {

        @RepeatedTest(10)
        @DisplayName("providerType, providerId로 SocialConnection 엔티티 존재 여부 확인")
        void existsByProviderTypeAndProviderId() {
            // given
            User             user             = em.persistAndFlush(TestUtils.createUser());
            SocialConnection socialConnection = em.persistAndFlush(TestUtils.createSocialConnection(user));
            ProviderType     providerType     = socialConnection.getProviderType();
            String           providerId       = socialConnection.getProviderId();

            // when
            boolean exists = socialConnectionRepository.existsByProviderTypeAndProviderId(providerType, providerId);

            // then
            assertTrue(exists, "SocialConnection 엔티티가 존재해야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("providerType, 존재하지 않는 providerId로 SocialConnection 엔티티 존재 여부 확인")
        void existsByProviderTypeAndProviderId_unknownProviderId(@NotNull ProviderType providerType,
                                                                 @NotBlank final String unknownProviderId) {
            // when
            boolean exists = socialConnectionRepository.existsByProviderTypeAndProviderId(
                    providerType, unknownProviderId
            );

            // then
            assertFalse(exists, "SocialConnection 엔티티가 존재하지 않아야 합니다.");
        }

    }

    @Nested
    @DisplayName("findByUserId() 테스트")
    class FindByUserIdTests {

        @RepeatedTest(10)
        @DisplayName("userId로 SocialConnection 엔티티 단 건 조회")
        void findByUserId() {
            // given
            User             user             = em.persistAndFlush(TestUtils.createUser());
            SocialConnection socialConnection = em.persistAndFlush(TestUtils.createSocialConnection(user));
            UUID             userId           = user.getId();

            // when
            SocialConnection findSocialConnection = socialConnectionRepository.findByUserId(userId).get();

            // then
            assertNotNull(findSocialConnection, "findSocialConnection는 null이 아니어야 합니다.");
            assertEquals(socialConnection.getUser(), findSocialConnection.getUser(), "user는 같아야 합니다.");
            assertEquals(socialConnection.getProviderType(), findSocialConnection.getProviderType(),
                         "providerType는 같아야 합니다.");
            assertEquals(socialConnection.getProviderId(), findSocialConnection.getProviderId(),
                         "providerId는 같아야 합니다.");
        }

        @ParameterizedTest
        @Repeat(10)
        @AutoSource
        @DisplayName("존재하지 않는 userId로 SocialConnection 엔티티 단 건 조회 시도")
        void findByUserId_unknownUserId(final UUID unknownUserId) {
            // when
            Optional<SocialConnection> opSocialConnection = socialConnectionRepository.findByUserId(unknownUserId);

            // then
            assertFalse(opSocialConnection.isPresent(), "조회된 SocialConnection 엔티티가 존재하지 않아야 합니다.");
        }

    }

    @Nested
    @DisplayName("SocialConnection 엔티티 필드 업데이트 테스트")
    class DirtyCheckingTests {

        @RepeatedTest(10)
        @DisplayName("SocialConnection 엔티티 필드 업데이트")
        void update() {
            // given
            User             user                  = em.persistAndFlush(TestUtils.createUser());
            String           beforeIdpRefreshToken = TestUtils.FAKER.internet().uuidv4();
            SocialConnection socialConnection      = TestUtils.createSocialConnection(user);
            socialConnection.updateIdpRefreshToken(beforeIdpRefreshToken);
            em.persistAndFlush(socialConnection);
            Long id = socialConnection.getId();

            // when
            SocialConnection findSocialConnection = socialConnectionRepository.findById(id).get();
            findSocialConnection.updateIdpRefreshToken(
                    String.format("updated%s", findSocialConnection.getIdpRefreshToken()));
            em.flush();

            // then
            SocialConnection updatedSocialConnection = em.find(SocialConnection.class, id);

            assertNotNull(updatedSocialConnection, "updatedSocialConnection는 존재하지 않아야 합니다.");
            assertEquals(findSocialConnection.getIdpRefreshToken(), updatedSocialConnection.getIdpRefreshToken(),
                         "idpRefreshToken은 업데이트된 값과 같아야 합니다.");
            assertNotEquals(beforeIdpRefreshToken, updatedSocialConnection.getIdpRefreshToken(),
                            "idpRefreshToken은 업데이트 이전 값과 달라야 합니다.");
        }

    }

    @Nested
    @DisplayName("deleteById() 테스트")
    class DeleteByIdTests {

        @RepeatedTest(10)
        @DisplayName("id로 SocialConnection 엔티티 삭제")
        void deleteById() {
            // given
            User             user             = em.persistAndFlush(TestUtils.createUser());
            SocialConnection socialConnection = em.persistAndFlush(TestUtils.createSocialConnection(user));
            Long             id               = socialConnection.getId();

            // when
            socialConnectionRepository.deleteById(id);
            em.flush();

            // then
            SocialConnection deletedSocialConnection = em.find(SocialConnection.class, id);

            assertNull(deletedSocialConnection, "deletedSocialConnection는 존재하지 않아야 합니다.");
        }

    }

}