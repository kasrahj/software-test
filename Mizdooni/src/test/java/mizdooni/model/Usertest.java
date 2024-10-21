package mizdooni.model;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.time.LocalTime;


public class Usertest {
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

        restaurant = new Restaurant("kburger",user,"fastfood",
        LocalTime.parse("08:00"),LocalTime.parse("23:00"),"test", address, "4.5");

        table = new Table(4, restaurant.getId(), 20);

        reservation = new Reservation(user, restaurant,
                table, LocalDateTime.now());
    }

    @Test
    void testGetUserName() {
        assertEquals("kasra", user.getUsername(), "Full name should be concatenated");
    }

    @Test
    void testPassEquality()
    {
        assertTrue(user.checkPassword("1234"), "Password should be correct");
    }

    @Test
    void testIdIncrement()
    {
        assertEquals(0, user.getId(), "Id should be incremented");
        User user2 = new User("kasra2", "1234",
                "kasra2@mail.com", new Address("IRAN", "Tehran", "kargar"),
                User.Role.client);
        assertEquals(1, user2.getId(), "Id should be incremented");
    }

    @Test
    void testAddReservation()
    {
        user.addReservation(reservation);
        assertEquals(1, user.getReservations().size(), "Reservation should be added");
        assertEquals(reservation, user.getReservation(reservation.getReservationNumber()), "Reservation should be added");
        assertTrue(user.checkReserved(restaurant));
    }

};
