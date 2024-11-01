package mizdooni.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class TableTest {

    private Table table;
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
        table = new Table(1, restaurant.getId(), 4);
    }

    @AfterEach
    public void tearDown() {
        table = null;
        restaurant = null;
        manager = null;
        address = null;
    }

    @ParameterizedTest
    @MethodSource("provideTableInitializationParams")
    public void testTableInitialization(int tableNumber, int seatsNumber, int expectedRestaurantId) {
        table.setTableNumber(tableNumber);
        table.setSeatsNumber(seatsNumber);
        assertEquals(tableNumber, table.getTableNumber());
        assertEquals(seatsNumber, table.getSeatsNumber());
//        assertEquals(expectedRestaurantId, restaurant.getId());
    }

    @ParameterizedTest
    @MethodSource("provideReservationTimesForAddReservation")
    public void testAddReservation(LocalDateTime reservationTime) {
        Reservation reservation = new Reservation(manager, restaurant, table, reservationTime);
        table.addReservation(reservation);

        List<Reservation> reservations = table.getReservations();
        assertEquals(1, reservations.size());
        assertEquals(reservation, reservations.get(0));
    }

    @ParameterizedTest
    @MethodSource("provideReservationTimesForIsReserved")
    public void testIsReserved(LocalDateTime reservationTime, LocalDateTime checkTime, boolean expectedResult) {
        Reservation reservation = new Reservation(manager, restaurant, table, reservationTime);
        table.addReservation(reservation);

        assertEquals(expectedResult, table.isReserved(checkTime));
    }

    @ParameterizedTest
    @MethodSource("provideTableNumberSetterParams")
    public void testTableNumberSetter(int newTableNumber) {
        table.setTableNumber(newTableNumber);
        assertEquals(newTableNumber, table.getTableNumber());
    }

    // MethodSource for parameterized tests

    private static Stream<Arguments> provideTableInitializationParams() {
        return Stream.of(
                Arguments.of(1, 4, 0),   // Table number 1, 4 seats, restaurant id 0
                Arguments.of(2, 6, 1),   // Table number 2, 6 seats
                Arguments.of(3, 8, 2)    // Table number 3, 8 seats
        );
    }

    private static Stream<Arguments> provideReservationTimesForAddReservation() {
        return Stream.of(
                Arguments.of(LocalDateTime.now().plusDays(1)),
                Arguments.of(LocalDateTime.now().plusDays(2)),
                Arguments.of(LocalDateTime.now().plusDays(3))
        );
    }

    private static Stream<Arguments> provideReservationTimesForIsReserved() {
        return Stream.of(
                Arguments.of(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1), true),  // Reservation at the same time
                Arguments.of(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1), false), // No reservation at check time
                Arguments.of(LocalDateTime.now().plusHours(5), LocalDateTime.now().plusHours(5), true), // Reservation at specific hour
                Arguments.of(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), false)   // Reservation at different day
        );
    }

    private static Stream<Arguments> provideTableNumberSetterParams() {
        return Stream.of(
                Arguments.of(5),
                Arguments.of(10),
                Arguments.of(15)
        );
    }
}
