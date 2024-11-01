package mizdooni.controllers;

import mizdooni.exceptions.*;
import mizdooni.model.*;
import mizdooni.response.PagedList;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.RestaurantService;
import mizdooni.service.ReviewService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static mizdooni.controllers.ControllerUtils.*;

class ReviewControllerTest {

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    private User manager;
    private Address address;
    private Restaurant restaurant;


    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
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

    @AfterEach
    void tearDown()
    {
        manager = null;
        address = null;
        restaurant = null;
    }

    @Test
    void testGetReviews_Successful() throws RestaurantNotFound {
        int page = 1;

        PagedList<Review> mockReviews = new PagedList<>(restaurant.getReviews(), page, 5);

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);
        when(reviewService.getReviews(restaurant.getId(), page)).thenReturn(mockReviews);

        Response response = reviewController.getReviews(restaurant.getId(), page);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(mockReviews, response.getData());
        assertEquals(response.getMessage(),"reviews for restaurant (" + restaurant.getId() + "): " + restaurant.getName());
        verify(restaurantService).getRestaurant(restaurant.getId());
        verify(reviewService).getReviews(restaurant.getId(), page);
    }
    @Test
    void testGetReviews_RestaurantNotFound() {
        int page = 1;

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(null);

        ResponseException exception = assertThrows(ResponseException.class, () ->
                reviewController.getReviews(restaurant.getId(), page)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(restaurantService).getRestaurant(restaurant.getId());
        verifyNoInteractions(reviewService);
    }

    @Test
    void testGetReviews_ReviewServiceException() throws RestaurantNotFound {
        int page = 1;

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);
        when(reviewService.getReviews(restaurant.getId(), page)).thenThrow(new RuntimeException());

        ResponseException exception = assertThrows(ResponseException.class, () ->
                reviewController.getReviews(restaurant.getId(), page)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(restaurantService).getRestaurant(restaurant.getId());
        verify(reviewService).getReviews(restaurant.getId(), page);
    }

    @Test
    void testAddReview_Successful() throws UserNotFound, ManagerCannotReview, UserHasNotReserved, RestaurantNotFound, InvalidReviewRating {
        Map<String, Object> params = new HashMap<>();
        params.put("comment", "Great food!");
        Map<String, Number> ratingMap = new HashMap<>();
        ratingMap.put("food", 4.5);
        ratingMap.put("service", 4.0);
        ratingMap.put("ambiance", 3.5);
        ratingMap.put("overall", 4.0);
        params.put("rating", ratingMap);

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);

        Response response = reviewController.addReview(restaurant.getId(), params);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("review added successfully", response.getMessage());
        verify(restaurantService).getRestaurant(restaurant.getId());
        verify(reviewService).addReview(eq(restaurant.getId()), any(Rating.class), eq("Great food!"));
    }

    @Test
    void testAddReview_RestaurantNotFound() {
        Map<String, Object> params = new HashMap<>();
        params.put("comment", "Great food!");
        Map<String, Number> ratingMap = new HashMap<>();
        ratingMap.put("food", 4.5);
        ratingMap.put("service", 4.0);
        ratingMap.put("ambiance", 3.5);
        ratingMap.put("overall", 4.0);
        params.put("rating", ratingMap);

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(null);

        ResponseException exception = assertThrows(ResponseException.class, () ->
                reviewController.addReview(restaurant.getId(), params)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(restaurantService).getRestaurant(restaurant.getId());
        verifyNoInteractions(reviewService);
    }

    @Test
    void testAddReview_UserNotFound() throws RestaurantNotFound, UserNotFound, ManagerCannotReview, UserHasNotReserved, InvalidReviewRating {
        Map<String, Object> params = new HashMap<>();
        params.put("comment", "Great food!");
        Map<String, Number> ratingMap = new HashMap<>();
        ratingMap.put("food", 4.5);
        ratingMap.put("service", 4.0);
        ratingMap.put("ambiance", 3.5);
        ratingMap.put("overall", 4.0);
        params.put("rating", ratingMap);

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);
        doThrow(new UserNotFound()).when(reviewService).addReview(eq(restaurant.getId()), any(Rating.class), eq("Great food!"));

        ResponseException exception = assertThrows(ResponseException.class, () ->
                reviewController.addReview(restaurant.getId(), params)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(restaurantService).getRestaurant(restaurant.getId());
        verify(reviewService).addReview(eq(restaurant.getId()), any(Rating.class), eq("Great food!"));
    }

    @Test
    void testAddReview_MissingParams()
    {
        Map<String, Object> params = new HashMap<>();

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);

        ResponseException exception = assertThrows(ResponseException.class, () ->
                reviewController.addReview(restaurant.getId(), params)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_MISSING, exception.getMessage());
        verify(restaurantService).getRestaurant(restaurant.getId());
        verifyNoInteractions(reviewService);
    }

    @Test
    void testAddReview_InvalidParamTypes() {
        Map<String, Object> params = new HashMap<>();
        params.put("comment", "Great food!");
        Map<String, Object> ratingMap = new HashMap<>();
        ratingMap.put("food", "high");  // Invalid type
        ratingMap.put("service", 4.0);
        ratingMap.put("ambiance", 3.5);
        ratingMap.put("overall", 4.0);
        params.put("rating", ratingMap);

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);

        ResponseException exception = assertThrows(ResponseException.class, () ->
                reviewController.addReview(restaurant.getId(), params)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_BAD_TYPE, exception.getMessage());
        verify(restaurantService).getRestaurant(restaurant.getId());
        verifyNoInteractions(reviewService);
    }
}
