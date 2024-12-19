package mizdooni.stepdefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import mizdooni.model.*;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;

public class RestaurantGetAverageRating {

    private Restaurant restaurant;
    private User user;
    private Address address;
    private User manager;

    private Rating average_rating;


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
        average_rating = null;
    }

    @Given("Restaurant doesn't have any reviews")
    public void restaurant_does_not_have_any_reviews() {
        List<Review> reviews = restaurant.getReviews();
        Assertions.assertTrue(reviews.isEmpty());
    }
    @When("Get the average rating of the restaurant")
    public void get_the_average_rating_of_the_restaurant() {
        average_rating = restaurant.getAverageRating();
    }
    @Then("I should see zero as the average rating")
    public void i_should_see_as_the_average_rating_zero()  {
        Assertions.assertEquals(0, average_rating.food);
        Assertions.assertEquals(0, average_rating.service);
        Assertions.assertEquals(0, average_rating.ambiance);
        Assertions.assertEquals(0, average_rating.overall);

    }

    @Given("Restaurant has reviews with ratings {string}")
    public void restaurant_has_reviews_with_ratings(String ratings) {
        List<Integer> ratingList = Arrays.stream(ratings.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();
        for (int rating : ratingList) {
            Rating ratingObj = Rating.RatingCreator(rating, rating, rating, rating);
            String name = "user" + rating;
            User user = new User(name, "password", "email", new Address("Iran", "Tehran", "1st street"), User.Role.client);
            Review review = new Review(user, ratingObj, "Good review", null);
            restaurant.addReview(review);
        }
    }

    @Then("I should see {double} as the average rating")
    public void i_should_see_as_the_average_rating(Double average) {
        Assertions.assertEquals(average, average_rating.food);
        Assertions.assertEquals(average, average_rating.service);
        Assertions.assertEquals(average, average_rating.ambiance);
        Assertions.assertEquals(average, average_rating.overall);
    }


}
