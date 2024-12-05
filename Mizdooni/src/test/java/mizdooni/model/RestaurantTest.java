package mizdooni.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class RestaurantTest {

    private Restaurant restaurant;
    private User manager;
    private Address address;

    @BeforeEach
    public void setUp() {
        manager = new User("Bardia", "123", "bardia@gmail.com", new Address("Iran", "Tehran", "1st street"), User.Role.manager);
        address = new Address("Iran", "Tehran", "2nd street");
        restaurant = new Restaurant(
                "telepizza",
                manager,
                "Iranian",
                LocalTime.of(9, 0),
                LocalTime.of(22, 0),
                "behtarin pizza ha",
                address,
                "link1"
        );
    }

    @AfterEach
    public void tearDown() {
        restaurant = null;
        manager = null;
        address = null;
    }

    @ParameterizedTest
    @MethodSource("provideRestaurantInitializationParams")
    public void testRestaurantInitialization(String name, String type, LocalTime startTime, LocalTime endTime, String description, String link) {
        restaurant = new Restaurant(name, manager, type, startTime, endTime, description, address, link);
        assertNotNull(restaurant);
        assertEquals(name, restaurant.getName());
        assertEquals(type, restaurant.getType());
        assertEquals(manager, restaurant.getManager());
        assertEquals(startTime, restaurant.getStartTime());
        assertEquals(endTime, restaurant.getEndTime());
        assertEquals(address, restaurant.getAddress());
    }

    @ParameterizedTest
    @MethodSource("provideAddTableParams")
    public void testAddTable(int tableNumber, int seatsNumber) {
        Table table = new Table(tableNumber, restaurant.getId(), seatsNumber);
        restaurant.addTable(table);

        List<Table> tables = restaurant.getTables();
        assertEquals(1, tables.size());
        assertEquals(1, tables.get(0).getTableNumber());
        assertEquals(seatsNumber, tables.get(0).getSeatsNumber());
    }

    @ParameterizedTest
    @MethodSource("provideGetTableParams")
    public void testGetTable(int tableNumber1, int seatsNumber1, int tableNumber2, int seatsNumber2, int retrieveTableNumber) {
        Table table1 = new Table(tableNumber1, restaurant.getId(), seatsNumber1);
        Table table2 = new Table(tableNumber2, restaurant.getId(), seatsNumber2);
        restaurant.addTable(table1);
        restaurant.addTable(table2);

        Table retrievedTable = restaurant.getTable(retrieveTableNumber);
        assertNotNull(retrievedTable);
        assertEquals(retrieveTableNumber, retrievedTable.getTableNumber());
    }

    @ParameterizedTest
    @MethodSource("provideAddReviewParams")
    public void testAddReview(User client, Rating rating, String comment) {
        Review review = new Review(client, rating, comment, LocalDateTime.now());
        restaurant.addReview(review);

        List<Review> reviews = restaurant.getReviews();
        assertEquals(1, reviews.size());
        assertEquals(review, reviews.get(0));
    }

    @ParameterizedTest
    @MethodSource("provideUpdateReviewParams")
    public void testUpdateReview(User client, Rating initialRating, Rating updatedRating, String updatedComment) {
        Review initialReview = new Review(client, initialRating, "initial comment", LocalDateTime.now());
        restaurant.addReview(initialReview);

        Review updatedReview = new Review(client, updatedRating, updatedComment, LocalDateTime.now());
        restaurant.addReview(updatedReview);

        List<Review> reviews = restaurant.getReviews();
        assertEquals(1, reviews.size());
        assertEquals(updatedReview, reviews.get(0));
    }

    @ParameterizedTest
    @MethodSource("provideAverageRatingParams")
    public void testGetAverageRating(List<Review> reviews, Rating expectedAverageRating) {
        for (Review review : reviews) {
            restaurant.addReview(review);
        }

        Rating averageRating = restaurant.getAverageRating();
        assertEquals(expectedAverageRating.food, averageRating.food);
        assertEquals(expectedAverageRating.service, averageRating.service);
        assertEquals(expectedAverageRating.ambiance, averageRating.ambiance);
        assertEquals(expectedAverageRating.overall, averageRating.overall);
    }

    @ParameterizedTest
    @MethodSource("provideStarCountParams")
    public void testGetStarCount(List<Review> reviews, int expectedStarCount) {
        for (Review review : reviews) {
            restaurant.addReview(review);
        }

        int starCount = restaurant.getStarCount();
        assertEquals(expectedStarCount, starCount);
    }

    @ParameterizedTest
    @MethodSource("provideMaxSeatsNumberParams")
    public void testGetMaxSeatsNumber(List<Table> tables, int expectedMaxSeats) {
        for (Table table : tables) {
            restaurant.addTable(table);
        }

        assertEquals(expectedMaxSeats, restaurant.getMaxSeatsNumber());
    }

    @Test
    public void testIdGeneration() {
        Restaurant anotherRestaurant = new Restaurant(
                "ataavich",
                new User("Kasra", "321", "kasra@gmail.com", address, User.Role.manager),
                "Iranian",
                LocalTime.of(10, 0),
                LocalTime.of(21, 0),
                "behtarin burgur ha",
                address,
                "link2"
        );

        assertNotEquals(restaurant.getId(), anotherRestaurant.getId());
    }

    // MethodSource providers

    private static Stream<Arguments> provideRestaurantInitializationParams() {
        return Stream.of(
                Arguments.of("telepizza", "Iranian", LocalTime.of(9, 0), LocalTime.of(22, 0), "best pizzas", "link1"),
                Arguments.of("ataavich", "Fast Food", LocalTime.of(10, 0), LocalTime.of(23, 0), "best burgers", "link2")
        );
    }

    private static Stream<Arguments> provideAddTableParams() {
        return Stream.of(
                Arguments.of(1, 4),
                Arguments.of(2, 6),
                Arguments.of(3, 10)
        );
    }

    private static Stream<Arguments> provideGetTableParams() {
        return Stream.of(
                Arguments.of(1, 4, 2, 6, 1),
                Arguments.of(1, 4, 2, 6, 2)
        );
    }

    private static Stream<Arguments> provideAddReviewParams() {
        return Stream.of(
                Arguments.of(new User("Amir", "1234", "Amir@gmail.com", new Address("Iran", "Tehran", "2nd street"), User.Role.client),
                        Rating.RatingCreator(5, 4, 4, 5), "good"),
                Arguments.of(new User("Sara", "5678", "Sara@gmail.com", new Address("Iran", "Tehran", "3rd street"), User.Role.client),
                        Rating.RatingCreator(4, 3, 3, 4), "okay")
        );
    }

    private static Stream<Arguments> provideUpdateReviewParams() {
        return Stream.of(
                Arguments.of(new User("Amir", "1234", "Amir@gmail.com", new Address("Iran", "Tehran", "2nd street"), User.Role.client),
                        Rating.RatingCreator(4, 3, 3, 4), Rating.RatingCreator(5, 5, 5, 5), "excellent")
        );
    }

    private static Stream<Arguments> provideAverageRatingParams() {
        return Stream.of(
                Arguments.of(
                        List.of(
                                new Review(new User("Ahmad", "0912", "Ahmad@gmail.com", new Address("Iran", "Tehran", "2nd street"), User.Role.client),
                                        Rating.RatingCreator(4, 4, 4, 4), "good", LocalDateTime.now()),
                                new Review(new User("Reza", "0901", "Reza@gmail.com", new Address("Iran", "Tehran", "3rd street"), User.Role.client),
                                        Rating.RatingCreator(5, 5, 5, 5), "excellent", LocalDateTime.now())
                        ),
                        Rating.RatingCreator(4.5, 4.5, 4.5, 4.5)
                )
        );
    }

    private static Stream<Arguments> provideStarCountParams() {
        return Stream.of(
                Arguments.of(
                        List.of(
                                new Review(new User("Ahmad", "0912", "Ahmad@gmail.com", new Address("Iran", "Tehran", "2nd street"), User.Role.client),
                                        Rating.RatingCreator(4, 4, 4, 4), "good", LocalDateTime.now()),
                                new Review(new User("Reza", "0901", "Reza@gmail.com", new Address("Iran", "Tehran", "3rd street"), User.Role.client),
                                        Rating.RatingCreator(5, 5, 5, 5), "excellent", LocalDateTime.now())
                        ),
                        5
                )
        );
    }

    private static Stream<Arguments> provideMaxSeatsNumberParams() {
        return Stream.of(
                Arguments.of(List.of(new Table(1, 0, 4), new Table(2, 0, 6), new Table(3, 0, 10)), 10),
                Arguments.of(List.of(new Table(1, 0, 2), new Table(2, 0, 8), new Table(3, 0, 12)), 12),
                Arguments.of(List.of(), 0)
        );
    }
}
