package mizdooni.controllers;

import mizdooni.model.Rating;
import mizdooni.model.Restaurant;
import mizdooni.model.Review;
import mizdooni.response.PagedList;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.RestaurantService;
import mizdooni.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewControllerTest {

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetReviews_Successful() {
        int restaurantId = 1;
        int page = 1;

        Restaurant mockRestaurant = new Restaurant();
        mockRestaurant.setId(restaurantId);
        mockRestaurant.setName("Test Restaurant");

        PagedList<Review> mockReviews = new PagedList<>();
        mockReviews.setList(Arrays.asList(new Review(), new Review()));

        when(restaurantService.findById(restaurantId)).thenReturn(mockRestaurant);
        when(reviewService.getReviews(restaurantId, page)).thenReturn(mockReviews);

        Response response = reviewController.getReviews(restaurantId, page);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(mockReviews, response.getData());
        assertTrue(response.getMessage().contains("reviews for restaurant (1)"));
        verify(restaurantService).findById(restaurantId);
        verify(reviewService).getReviews(