package mizdooni.stepdefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import mizdooni.model.*;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Assertions;


public class UserAddReservation {

    private User user;
    private Restaurant restaurant;
    private Address address;
    private User manager;
    private Reservation reservation;

    private int reservationCount = 0;

    @Before
    public void setup() {
        user = new User("user", "password", "email", new Address("Iran", "Tehran", "1st street"), User.Role.client);
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
        reservation = new Reservation(user, restaurant, new Table(4, restaurant.getId(), 20), LocalDateTime.now());
    }

    @After
    public void teardown() {
        user = null;
        restaurant = null;
        manager = null;
        address = null;
    }


    @Given("User is logged in")
    public void user_is_logged_in() {
        Assertions.assertNotNull(user);
        reservationCount = user.getReservations().size();
    }

    @When("User add a reservation")
    public void user_add_a_reservation() {
        user.addReservation(reservation);
    }

    @Then("User should see the reservation in the list")
    public void User_should_see_the_reservation_in_the_list() {
        List<Reservation> reservations = user.getReservations();
        Assertions.assertTrue(reservations.stream().anyMatch(r -> r.equals(reservation)));
        Assertions.assertEquals(reservationCount + 1, reservations.size());
        Assertions.assertTrue(user.checkReserved(restaurant));
        Assertions.assertEquals(reservation, user.getReservation(reservation.getReservationNumber()));
    }

}
