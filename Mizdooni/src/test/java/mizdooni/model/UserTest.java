package mizdooni.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;

public class UserTest {

    @Autowired
    User user;
    @Autowired
    Reservation reservation;

    Address address;
    @Autowired
    Restaurant restaurant;

    Table table;

    @BeforeEach
    void setUp() {
        address = new Address("IRAN", "Tehran", "kargar");

        user = new User("kasra", "1234",
                "kasra@mail.com", address, User.Role.manager);

        restaurant = new Restaurant("kburger", user, "fastfood",
                LocalTime.parse("08:00"), LocalTime.parse("23:00"), "test", address, "4.5");

        table = new Table(4, restaurant.getId(), 20);

        reservation = new Reservation(user, restaurant,
                table, LocalDateTime.now());
    }
    @AfterEach
    void tearDown() {
        user = null;
        reservation = null;
        address = null;
        restaurant = null;
        table = null;
    }

    @ParameterizedTest
    @MethodSource("provideUsernamesForTest")
    void testGetUserName(String username, String expected) {
        user.setUsername(username);
        assertEquals(expected, user.getUsername(), "Usernames should match");
    }

    @ParameterizedTest
    @MethodSource("providePasswordsForTest")
    void testPassEquality(String inputPassword, boolean isValid) {
        assertEquals(isValid, user.checkPassword(inputPassword), "Password check should return the correct result");
    }

//    @ParameterizedTest
//    @MethodSource("provideUsersForIdIncrementTest")
//    void testIdIncrement(User firstUser, User secondUser, int expectedIdFirst, int expectedIdSecond) {
//        assertEquals(expectedIdFirst, firstUser.getId(), "First user's ID should match");
//        assertEquals(expectedIdSecond, secondUser.getId(), "Second user's ID should be incremented");
//    }

    @Test
    void testAddReservation() {
        user.addReservation(reservation);
        assertEquals(1, user.getReservations().size(), "Reservation should be added");
        assertEquals(reservation, user.getReservation(reservation.getReservationNumber()), "Reservation should be retrievable by reservation number");
        assertTrue(user.checkReserved(restaurant), "User should have a reservation for the restaurant");
    }

    // MethodSource for parameterized tests
    private static Stream<Arguments> provideUsernamesForTest() {
        return Stream.of(
                Arguments.of("kasra", "kasra"),
                Arguments.of("john_doe", "john_doe"),
                Arguments.of("", ""),  // Empty username
                Arguments.of("admin123", "admin123")
        );
    }

    private static Stream<Arguments> providePasswordsForTest() {
        return Stream.of(
                Arguments.of("1234", true),  // Correct password
                Arguments.of("wrong_password", false),  // Incorrect password
                Arguments.of("", false),  // Empty password
                Arguments.of("12345", false)  // Incorrect password with similar value
        );
    }

    private static Stream<Arguments> provideUsersForIdIncrementTest() {
        Address address = new Address("IRAN", "Tehran", "kargar");

        User user1 = new User("kasra", "1234", "kasra@mail.com", address, User.Role.manager);
        User user2 = new User("kasra2", "1234", "kasra2@mail.com", address, User.Role.client);

        return Stream.of(
                Arguments.of(user1, user2, 0, 1)  // First user ID should be 0, second should be incremented to 1
        );
    }
}
