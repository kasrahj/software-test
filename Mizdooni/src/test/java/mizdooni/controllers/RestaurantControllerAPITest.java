package mizdooni.controllers;

import static mizdooni.controllers.ControllerUtils.TIME_FORMATTER;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.any;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;


import org.hamcrest.CoreMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import mizdooni.model.Address;
import mizdooni.model.Restaurant;
import mizdooni.model.RestaurantSearchFilter;
import mizdooni.model.User;
import mizdooni.response.PagedList;
import mizdooni.service.RestaurantService;
import mizdooni.service.UserService;
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class RestaurantControllerAPITest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private RestaurantService restaurantService;
    @MockBean
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    private static User manager;

    private static Address address;

    private static Restaurant CreateRestaurant() {
        Restaurant restaurant = new Restaurant(
                "telepizza",
                manager,
                "Iranian",
                LocalTime.of(9, 0),
                LocalTime.of(22, 0),
                "behtarin pizza ha",
                address,
                "link1"
        );
        return restaurant;
    }

    private void checkRestaurantResponse(ResultActions response, String jsonBasePath, Restaurant restaurant)
            throws Exception {
        response
                .andExpect(jsonPath(jsonBasePath + ".id", CoreMatchers.is(restaurant.getId())))
                .andExpect(jsonPath(jsonBasePath + ".name", CoreMatchers.is(restaurant.getName())))
                .andExpect(jsonPath(jsonBasePath + ".startTime", CoreMatchers.is(restaurant.getStartTime().format(TIME_FORMATTER))))
                .andExpect(jsonPath(jsonBasePath + ".endTime", CoreMatchers.is(restaurant.getEndTime().format(TIME_FORMATTER))))
                .andExpect(jsonPath(jsonBasePath + ".address.country", CoreMatchers.is(restaurant.getAddress().getCountry())))
                .andExpect(jsonPath(jsonBasePath + ".address.city", CoreMatchers.is(restaurant.getAddress().getCity())))
                .andExpect(jsonPath(jsonBasePath + ".address.street", CoreMatchers.is(restaurant.getAddress().getStreet())))
                .andExpect(jsonPath(jsonBasePath + ".starCount", CoreMatchers.is(restaurant.getStarCount())))
                .andExpect(jsonPath(jsonBasePath + ".averageRating.food", CoreMatchers.is(restaurant.getAverageRating().food)))
                .andExpect(jsonPath(jsonBasePath + ".averageRating.service", CoreMatchers.is(restaurant.getAverageRating().service)))
                .andExpect(jsonPath(jsonBasePath + ".averageRating.ambiance", CoreMatchers.is(restaurant.getAverageRating().ambiance)))
                .andExpect(jsonPath(jsonBasePath + ".averageRating.overall", CoreMatchers.is(restaurant.getAverageRating().overall)))
                .andExpect(jsonPath(jsonBasePath + ".maxSeatsNumber", CoreMatchers.is(restaurant.getMaxSeatsNumber())))
                .andExpect(jsonPath(jsonBasePath + ".managerUsername", CoreMatchers.is(restaurant.getManager().getUsername())))
                .andExpect(jsonPath(jsonBasePath + ".totalReviews", CoreMatchers.is(restaurant.getReviews().size())));
    }

    @BeforeAll
    static void setUp() {
        manager = new User("Bardia", "123", "bardia@gmail.com", new Address("Iran", "Tehran", "1st street"), User.Role.manager);
        address = new Address("Iran", "Tehran", "2nd street");
    }

    @Nested
    class GetRestaurant {

        @Test
        void GetRestaurantShouldReturnRestaurant_whenRestaurantExists() throws Exception {
            Restaurant restaurant = CreateRestaurant();
            int restaurantId = restaurant.getId();
            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);

            ResultActions response = mockMvc.perform(get("/restaurants/{restaurantId}", Integer.toString(restaurantId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", CoreMatchers.is(true)))
                    .andExpect(jsonPath("$.message", CoreMatchers.is("restaurant found")))
                    .andExpect(jsonPath("$.data").isMap());

            checkRestaurantResponse(response, "$.data", restaurant);
        }

        @Test
        void GetRestaurantShouldReturnNotFoundResponse_whenRestaurantDoesNotExists() throws Exception {
            int restaurantId = -1;
            when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);

            mockMvc.perform(get("/restaurants/{restaurantId}", Integer.toString(restaurantId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", CoreMatchers.is(false)))
                    .andExpect(jsonPath("$.message", CoreMatchers.is("restaurant not found")));
        }

        @Test
        void shouldReturnBadRequest_whenRestaurantIdIsInvalid() throws Exception {
            mockMvc.perform(get("/restaurants/{restaurantId}", "invalidId")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetRestaurants {

        static Object[][] getRestaurantsParameters() {
            List<Restaurant> restaurants = List.of(CreateRestaurant(),CreateRestaurant());
            RestaurantSearchFilter filter1 = new RestaurantSearchFilter();
//            filter1.setName("burger land");
//            filter1.setSort("rating");
//            filter1.setOrder("asc");

            return new Object[][] {
                    {
                            1,
                            Map.of(
                                    "name",  "Burger land",
                                    "sort", "rating",
                                    "order", "asc"
                            ),
                            restaurants,
                    },
                    {
                            1,
                            Map.of(
                                    "type", "italian",
                                    "sort", "reviews",
                                    "order", "desc"
                            ),
                            restaurants
                    },
                    {
                            3,
                            Map.of(
                                    "location", "Tehranpars",
                                    "type", "seafood"
                            ),
                            restaurants
                    }
            };
        }
        @ParameterizedTest
        @MethodSource("getRestaurantsParameters")
        void GetRestaurantsShouldReturnCorrectListOfRestaurants(int page, Map<String, String> params,
                                                  List<Restaurant> restaurants) throws Exception {
            PagedList<Restaurant> listRestaurants = new PagedList<>(restaurants, page, 10);
            when(restaurantService.getRestaurants(eq(page), any(RestaurantSearchFilter.class)))
                    .thenReturn(listRestaurants);

            ResultActions response = mockMvc.perform(get("/restaurants")
                            .param("page", Integer.toString(page))
                            .params(new LinkedMultiValueMap<>() {{ params.forEach((key, value) -> add(key, value)); }})
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", CoreMatchers.is(true)))
                    .andExpect(jsonPath("$.message", CoreMatchers.is("restaurants listed")))
                    .andExpect(jsonPath("$.data").isMap())
                    .andExpect(jsonPath("$.data.page", CoreMatchers.is(page)))
                    .andExpect(jsonPath("$.data.hasNext", CoreMatchers.is(listRestaurants.hasNext())))
                    .andExpect(jsonPath("$.data.totalPages", CoreMatchers.is(listRestaurants.totalPages())))
                    .andExpect(jsonPath("$.data.pageList").isArray());
            for (int i = 0; i < listRestaurants.getPageList().size(); i++) {
                Restaurant restaurant = listRestaurants.getPageList().get(i);
                checkRestaurantResponse(response, "$.data.pageList[" + i + "]", restaurant);
            }
        }

        static Object[][] getRestaurantsInvalidParameters() {
            return new Object[][] {
                    {
                            "invalid",
                            Map.of(
                                    "name", "chaman",
                                    "location", "niavaran"
                            )
                    },
                    {
                            "1",
                            Map.of(

                                    "sort", "reviews",
                                    "order", "invalid order"
                            )
                    },
                    {
                            "1",
                            Map.of(
                                    "sort", "invalid sort"
                            )
                    },
            };
        }
        @ParameterizedTest
        @MethodSource("getRestaurantsInvalidParameters")
        void GetRestaurantsShouldReturnBadRequest_whenParametersIsMissedOrInvalid(String page,Map<String, String> params) throws Exception {
            mockMvc.perform(get("/restaurants")
                            .param("page", page)
                            .params(new LinkedMultiValueMap<>() {{ params.forEach((key, value) -> add(key, value)); }})
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }
}
