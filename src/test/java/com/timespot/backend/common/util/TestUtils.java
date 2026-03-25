package com.timespot.backend.common.util;

import static lombok.AccessLevel.PRIVATE;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.instantiator.Instantiator;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.timespot.backend.domain.favorite.model.Favorite;
import com.timespot.backend.domain.history.model.VisitingHistory;
import com.timespot.backend.domain.history.model.VisitingHistoryPlace;
import com.timespot.backend.domain.place.model.Place;
import com.timespot.backend.domain.place.model.Station;
import com.timespot.backend.domain.user.model.MapApi;
import com.timespot.backend.domain.user.model.ProviderType;
import com.timespot.backend.domain.user.model.SocialConnection;
import com.timespot.backend.domain.user.model.User;
import com.timespot.backend.domain.user.model.UserRole;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import lombok.NoArgsConstructor;
import net.datafaker.Faker;

/**
 * PackageName : com.timespot.backend.common.util
 * FileName    : TestUtils
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description : 테스트용 유틸리티 클래스 (엔티티 생성, Faker 등)
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
public final class TestUtils {

    public static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
                                                                    .objectIntrospector(
                                                                            new FailoverIntrospector(
                                                                                    List.of(FieldReflectionArbitraryIntrospector.INSTANCE,
                                                                                            ConstructorPropertiesArbitraryIntrospector.INSTANCE,
                                                                                            BuilderArbitraryIntrospector.INSTANCE,
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
                                                      .parameter(MapApi.class, "mapApi")
                                                      .parameter(UserRole.class, "role"))
                             .setLazy("email", () -> FAKER.internet().safeEmailAddress())
                             .setLazy("nickname", () -> FAKER.credentials().username().replace(".", "").substring(0, 5))
                             .set("mapApi", MapApi.APPLE)
                             .set("role", UserRole.USER)
                             .sampleList(size);
    }

    public static User createUser() {
        return createUsers(1).get(0);
    }

    public static List<SocialConnection> createSocialConnections(final List<User> users) {
        return users.stream()
                    .map(user -> SocialConnection.of(user,
                                                     ProviderType.APPLE,
                                                     FAKER.internet().uuidv4(),
                                                     FAKER.internet().uuidv4()))
                    .toList();
    }

    public static SocialConnection createSocialConnection(final User user) {
        return createSocialConnections(List.of(user)).get(0);
    }

    public static List<Station> createStations(final int size) {
        return FIXTURE_MONKEY.giveMeBuilder(Station.class)
                             .instantiate(Instantiator.constructor().field())
                             .setNull("id")
                             .setLazy("name", () -> String.format("%s Station", FAKER.address().cityName()))
                             .setLazy("address", () -> FAKER.address().fullAddress())
                             .setLazy("latitude", () -> Double.parseDouble(FAKER.address().latitude()))
                             .setLazy("longitude", () -> Double.parseDouble(FAKER.address().longitude()))
                             .set("isActive", true)
                             .sampleList(size);
    }

    public static Station createStation() {
        return createStations(1).get(0);
    }

    public static Station createStation(final String name) {
        return FIXTURE_MONKEY.giveMeBuilder(Station.class)
                             .instantiate(Instantiator.constructor().field())
                             .setNull("id")
                             .set("name", name)
                             .setLazy("address", () -> FAKER.address().fullAddress())
                             .setLazy("latitude", () -> Double.parseDouble(FAKER.address().latitude()))
                             .setLazy("longitude", () -> Double.parseDouble(FAKER.address().longitude()))
                             .set("isActive", true)
                             .sample();
    }

    public static List<Place> createPlaces(final int size) {
        return FIXTURE_MONKEY.giveMeBuilder(Place.class)
                             .instantiate(Instantiator.constructor().field())
                             .setNull("id")
                             .setLazy("googlePlaceId", () -> FAKER.internet().uuidv4())
                             .setLazy("name", () -> FAKER.commerce().productName())
                             .setLazy("address", () -> FAKER.address().fullAddress())
                             .setLazy("category", () -> FAKER.commerce().department())
                             .sampleList(size);
    }

    public static Place createPlace() {
        return createPlaces(1).get(0);
    }

    public static List<Favorite> createFavorites(final User user, final List<Station> stations) {
        return stations.stream()
                       .map(station -> Favorite.of(user, station))
                       .toList();
    }

    public static Favorite createFavorite(final User user, final Station station) {
        return createFavorites(user, List.of(station)).get(0);
    }

    public static List<VisitingHistory> createVisitingHistories(final User user,
                                                                final List<Station> stations,
                                                                final LocalDateTime startTime) {
        final LocalDateTime trainDepartureTime = startTime.plusMinutes(30);
        return stations.stream()
                       .map(station -> VisitingHistory.startJourney(user,
                                                                    station,
                                                                    startTime,
                                                                    trainDepartureTime))
                       .toList();
    }

    public static VisitingHistory createVisitingHistory(final User user,
                                                        final Station station,
                                                        final LocalDateTime startTime) {
        return createVisitingHistories(user, List.of(station), startTime).get(0);
    }

    public static VisitingHistoryPlace createVisitingHistoryPlace(final VisitingHistory visitingHistory,
                                                                  final Place place) {
        return VisitingHistoryPlace.of(visitingHistory, place);
    }

}
