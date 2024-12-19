package mizdooni.stepdefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import mizdooni.model.Address;
import mizdooni.model.Restaurant;
import mizdooni.model.Review;
import mizdooni.model.User;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Assertions;

public class RestaurantAddReview {
    private Review review;
    private Restaurant restaurant;
    private User user;
    private Address address;
    private User manager;
    private int reviewCount = 0;

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
        review = null;
        reviewCount = 0;
    }

    @Given("User doesn't have a review")
    public void user_does_not_have_a_review() {
        List<Review> reviews = restaurant.getReviews();
        Assertions.assertTrue(reviews.stream()
                .noneMatch(r -> r.getUser().equals(user)));
    }

    @When("I add a review")
    public void i_add_a_review() {
        review = new Review(user, null, "comment", null);
        restaurant.addReview(review);
    }

    @Then("the review should be added")
    public void the_review_should_be_added() {
        List<Review> reviews = restaurant.getReviews();
        for (Review r : reviews) {
            if (r.getUser().equals(user)) {
                Assertions.assertEquals(review, r);
                return;
            }
        }
        Assertions.assertEquals(reviews.size(), reviewCount + 1);
    }

    @Given("User already has a review")
    public void user_already_has_a_review() {
        Review oldReview = new Review(user, null, "old", null);
        restaurant.addReview(oldReview);
        Assertions.assertEquals(restaurant.getReviews().size(), reviewCount + 1);
        reviewCount = restaurant.getReviews().size();
        Assertions.assertTrue(restaurant.getReviews().stream()
                .anyMatch(r -> r.getUser().equals(user)));
    }

    @Then("the review should be updated")
    public void the_review_should_be_updated() {
        List<Review> reviews = restaurant.getReviews();
        for (Review r : reviews) {
            if (r.getUser().equals(user)) {
                Assertions.assertEquals(review, r);
                return;
            }
        }
        Assertions.assertEquals(reviews.size(), reviewCount);
    }
}
