package mizdooni.stepdefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import mizdooni.model.*;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;


public class UserAddReservation {

    private User user;
    private Restaurant restaurant;
    private Address address;
    private User manager;
    private List<Reservation> reservations = new ArrayList<>();

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
    }

    @After
    public void teardown() {
        user = null;
        restaurant = null;
        manager = null;
        address = null;
    }

    @Given("Users new reservation")
    public void users_new_reservation() {
        reservations.add(new Reservation(user, restaurant, new Table(4, restaurant.getId(), 20), LocalDateTime.now()));
    }
    @When("User add the reservation")
    public void user_add_the_reservation() {
        user.addReservation(reservations.get(0));
    }
    @Then("the reservation should be added with reservation number {int}")
    public void the_reservation_should_be_added_with_reservation_number(Integer int1) {
        List<Reservation> reservations = user.getReservations();
        Assertions.assertTrue(reservations.stream().anyMatch(r -> r.getReservationNumber() == int1));
        Assertions.assertEquals(reservationCount + 1, reservations.size());

    }

    @Given("Users has {int} reservations")
    public void users_has_reservations(Integer int1) {
        for (int i = 0; i < int1; i++) {
            reservations.add(new Reservation(user, restaurant, new Table(4, restaurant.getId(), 20), LocalDateTime.now()));
        }
        Assertions.assertEquals(int1, reservations.size());
    }
    @When("User add all the reservations")
    public void user_add_all_the_reservations() {
        for (Reservation r : reservations) {
            user.addReservation(r);
        }
    }
    @Then("they should be added with unique reservation numbers starting from {int}")
    public void they_should_be_added_with_unique_reservation_numbers_starting_from(Integer int1) {
        List<Reservation> reservations = user.getReservations();
        for (int i = 0; i < reservations.size(); i++) {
            Assertions.assertEquals(int1 + i, reservations.get(i).getReservationNumber());
        }
    }

}
