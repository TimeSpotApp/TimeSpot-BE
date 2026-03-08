package com.nomadspot.backend.common.util;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.instantiator.Instantiator;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.nomadspot.backend.domain.user.model.ProviderType;
import com.nomadspot.backend.domain.user.model.SocialConnection;
import com.nomadspot.backend.domain.user.model.User;
import com.nomadspot.backend.domain.user.model.UserRole;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.datafaker.Faker;

/**
 * PackageName : com.nomadspot.backend.common.util
 * FileName    : TestUtils
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestUtils {

    public static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
                                                                    .objectIntrospector(
                                                                            new FailoverIntrospector(
                                                                                    List.of(ConstructorPropertiesArbitraryIntrospector.INSTANCE,
                                                                                            BuilderArbitraryIntrospector.INSTANCE,
                                                                                            FieldReflectionArbitraryIntrospector.INSTANCE,
                                                                                            BeanArbitraryIntrospector.INSTANCE),
                                                                                    false
                                                                            )
                                                                    )
                                                                    .plugin(new JakartaValidationPlugin())
                                                                    .defaultNotNull(true)
                                                                    .nullableContainer(false)
                                                                    .nullableElement(false)
                                                                    .build();

    public static final Faker FAKER = new Faker(new Locale.Builder().setLanguage("en").build(), new Random());

    public static List<User> createUsers(final int size) {
        return FIXTURE_MONKEY.giveMeBuilder(User.class)
                             .instantiate(Instantiator.factoryMethod("of")
                                                      .parameter(String.class, "email")
                                                      .parameter(String.class, "nickname")
                                                      .parameter(UserRole.class, "role"))
                             .setLazy("email", () -> FAKER.internet().safeEmailAddress())
                             .setLazy("nickname", () -> FAKER.credentials().username().replace(".", "").substring(0, 5))
                             .set("role", UserRole.USER)
                             .sampleList(size);
    }

    public static User createUser() {
        return createUsers(1).get(0);
    }

    public static List<SocialConnection> createSocialConnections(final List<User> users) {
        return users.stream()
                    .map(user -> SocialConnection.of(user, ProviderType.APPLE, FAKER.internet().uuidv4()))
                    .toList();
    }

    public static SocialConnection createSocialConnection(final User user) {
        return createSocialConnections(List.of(user)).get(0);
    }

}
