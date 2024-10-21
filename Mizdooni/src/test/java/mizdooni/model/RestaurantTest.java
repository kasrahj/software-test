package mizdooni.model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
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

    @Test
    public void testRestaurantInitialization() {
        assertNotNull(restaurant);
        assertEquals("telepizza", restaurant.getName());
        assertEquals("Iranian", restaurant.getType());
        assertEquals(manager, restaurant.getManager());
        assertEquals(LocalTime.of(9, 0), restaurant.getStartTime());
        assertEquals(LocalTime.of(22, 0), restaurant.getEndTime());
        assertEquals(address, restaurant.getAddress());
    }

    @Test
    public void testAddTable() {
        Table table = new Table(1, restaurant.getId(), 4);
        restaurant.addTable(table);

        List<Table> tables = restaurant.getTables();
        assertEquals(1, tables.size());
        assertEquals(1, tables.get(0).getTableNumber());
        assertEquals(4, tables.get(0).getSeatsNumber());
    }

    @Test
    public void testGetTable() {
        Table table1 = new Table(1, restaurant.getId(), 4);
        Table table2 = new Table(2, restaurant.getId(), 6);
        restaurant.addTable(table1);
        restaurant.addTable(table2);

        Table retrievedTable = restaurant.getTable(2);
        assertNotNull(retrievedTable);
        assertEquals(2, retrievedTable.getTableNumber());
        assertEquals(6, retrievedTable.getSeatsNumber());
    }

    @Test
    public void testAddReview() {
        User client = new User("Amir", "1234", "Amir@gmail.com", address, User.Role.client);
        Rating rating = new Rating();
        rating.food = 5;
        rating.service = 4;
        rating.ambiance = 4;
        rating.overall = 5;
        Review review = new Review(client, rating, "good", LocalDateTime.now());
        restaurant.addReview(review);

        List<Review> reviews = restaurant.getReviews();
        assertEquals(1, reviews.size());
        assertEquals(review, reviews.get(0));
    }

    @Test
    public void testUpdateReview() {
        User client = new User("Amir", "1234", "Amir@gmail.com", address, User.Role.client);
        Rating rating = new Rating();
        rating.food = 4;
        rating.service = 3;
        rating.ambiance = 3;
        rating.overall = 4;

        Review initialReview = new Review(client, rating, "mid", LocalDateTime.now());
        restaurant.addReview(initialReview);

        // Update review by the same user
        Rating updatedRating = new Rating();
        updatedRating.food = 5;
        updatedRating.service = 5;
        updatedRating.ambiance = 5;
        updatedRating.overall = 5;
        Review updatedReview = new Review(client, rating, "good", LocalDateTime.now());
        restaurant.addReview(updatedReview);

        List<Review> reviews = restaurant.getReviews();
        assertEquals(1, reviews.size());
        assertEquals(updatedReview, reviews.get(0));
    }

    @Test
    public void testGetAverageRating() {
        User client1 = new User("Ahmad", "0912", "Ahmad@gmail.com", address, User.Role.client);
        User client2 = new User("Reza", "0901", "Reza@gmail.com", address, User.Role.client);
        Rating rating1 = new Rating();
        rating1.food = 4;
        rating1.service = 4;
        rating1.ambiance = 4;
        rating1.overall = 4;
        Rating rating2 = new Rating();
        rating2.food = 5;
        rating2.service = 5;
        rating2.ambiance = 5;
        rating2.overall = 5;
        Review review1 = new Review(client1, rating1, "good", LocalDateTime.now());
        Review review2 = new Review(client2, rating2, "excellent", LocalDateTime.now());
        restaurant.addReview(review1);
        restaurant.addReview(review2);

        Rating averageRating = restaurant.getAverageRating();
        assertEquals(4.5, averageRating.food);
        assertEquals(4.5, averageRating.service);
        assertEquals(4.5, averageRating.ambiance);
        assertEquals(4.5, averageRating.overall);
    }

    @Test
    public void testGetStarCount() {
        User client1 = new User("Ahmad", "0912", "Ahmad@gmail.com", address, User.Role.client);
        User client2 = new User("Reza", "0901", "Reza@gmail.com", address, User.Role.client);
        Rating rating1 = new Rating();
        rating1.food = 4;
        rating1.service = 4;
        rating1.ambiance = 4;
        rating1.overall = 4;
        Rating rating2 = new Rating();
        rating2.food = 5;
        rating2.service = 5;
        rating2.ambiance = 5;
        rating2.overall = 5;
        Review review1 = new Review(client1, rating1, "good", LocalDateTime.now());
        Review review2 = new Review(client2, rating2, "excellent", LocalDateTime.now());
        restaurant.addReview(review1);
        restaurant.addReview(review2);

        int starCount = restaurant.getStarCount();
        assertEquals(5, starCount); // Based on average rating logic
    }

    @Test
    public void testGetMaxSeatsNumber() {
        Table table1 = new Table(1, restaurant.getId(), 4);
        Table table2 = new Table(2, restaurant.getId(), 6);
        Table table3 = new Table(3, restaurant.getId(), 10);
        restaurant.addTable(table1);
        restaurant.addTable(table2);
        restaurant.addTable(table3);

        assertEquals(10, restaurant.getMaxSeatsNumber());
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
}
