import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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

    @Test
    public void testTableInitialization() {
        assertEquals(1, table.getTableNumber());
        assertEquals(4, table.getSeatsNumber());
        assertEquals(restaurant.getId(), table.getReservations().size());
    }

    @Test
    public void testAddReservation() {
        Reservation reservation = new Reservation(restaurant, LocalDateTime.now().plusDays(1));
        table.addReservation(reservation);

        List<Reservation> reservations = table.getReservations();
        assertEquals(1, reservations.size());
        assertEquals(reservation, reservations.get(0));
    }

    @Test
    public void testIsReserved() {
        LocalDateTime reservationTime = LocalDateTime.now().plusDays(1);
        Reservation reservation = new Reservation(restaurant, reservationTime);
        table.addReservation(reservation);

        assertTrue(table.isReserved(reservationTime)); // Table should be reserved at the same time
        assertFalse(table.isReserved(LocalDateTime.now().plusDays(1))); // Table should not be reserved at a different time ex: ( 1 day later )
    }

    @Test
    public void testTableNumberSetter() {
        table.setTableNumber(5);
        assertEquals(5, table.getTableNumber());
    }
}
