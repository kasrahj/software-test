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
import java.util.Set;

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

import mizdooni.exceptions.DuplicatedRestaurantName;
import mizdooni.exceptions.InvalidWorkingTime;
import mizdooni.exceptions.UserNotManager;
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

    @AfterAll
    static void teardown()
    {
        manager = null;
        address = null;
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
            List<Restaurant> restaurants = List.of(CreateRestaurant(), CreateRestaurant());
            RestaurantSearchFilter filter1 = new RestaurantSearchFilter();
//            filter1.setName("burger land");
//            filter1.setSort("rating");
//            filter1.setOrder("asc");

            return new Object[][]{
                    {
                            1,
                            Map.of(
                                    "name", "Burger land",
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
                            .params(new LinkedMultiValueMap<>() {{
                                params.forEach((key, value) -> add(key, value));
                            }})
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
            return new Object[][]{
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
        void GetRestaurantsShouldReturnBadRequest_whenParametersIsMissedOrInvalid(String page, Map<String, String> params) throws Exception {
            mockMvc.perform(get("/restaurants")
                            .param("page", page)
                            .params(new LinkedMultiValueMap<>() {{
                                params.forEach((key, value) -> add(key, value));
                            }})
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    class GetManagerRestaurants {

        static Object[][] getManagerRestaurantsValidManagerIdParameters() {
            return new Object[][]{
                    {
                            manager,
                            List.of(CreateRestaurant(), CreateRestaurant())
                    },
                    {
                            manager,
                            Collections.emptyList()
                    }
            };
        }

        @ParameterizedTest
        @MethodSource("getManagerRestaurantsValidManagerIdParameters")
        void GetManagerRestaurantsShouldReturnMangerRestaurants_whenRestaurantMangerIdIsValid(User manager, List<Restaurant> restaurants)
                throws Exception {
            int managerId = manager.getId();
            when(restaurantService.getManagerRestaurants(eq(managerId))).thenReturn(restaurants);
            when(userService.getManager(eq(managerId))).thenReturn(manager);
            ResultActions response = mockMvc.perform(get("/restaurants/manager/{managerId}", Integer.toString(managerId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", CoreMatchers.is("manager restaurants listed")))
                    .andExpect(jsonPath("$.success", CoreMatchers.is(true)))
                    .andExpect(jsonPath("$.data").isArray())
                    .andDo(print());
            for (int i = 0; i < restaurants.size(); i++) {
                Restaurant restaurant = restaurants.get(i);
                checkRestaurantResponse(response, "$.data[" + i + "]", restaurant);
            }
        }

        @Test
        void GetManagerRestaurantsShouldReturnBadRequest_whenManagerIdIsInvalid() throws Exception {
            int managerId = -1;
            when(userService.getManager(eq(managerId))).thenReturn(null);
            mockMvc.perform(get("/restaurants/manager/{managerId}", Integer.toString(managerId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void GetManagerRestaurantsShouldReturnBadRequest_whenManagerIdIsInvalidFormat() throws Exception {
            mockMvc.perform(get("/restaurants/manager/{managerId}", "invalid manager id")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    //
    @Nested
    class AddRestaurant {

        static Object[][] addRestaurantValidParamsParameters() {
            return new Object[][]{
                    {
                            Map.of(
                                    "name", "Burger Land",
                                    "type", "fastfood",
                                    "description", "best burgers in town",
                                    "startTime", "09:30",
                                    "endTime", "23:00",
                                    "address", Map.of(
                                            "city", "Tehran",
                                            "country", "Iran",
                                            "street", "2th"
                                    ),
                                    "image", "link1"
                            )
                    },
                    {
                            Map.of(
                                    "name", "chaman",
                                    "type", "dirtyfood",
                                    "description", "best dirty food in town",
                                    "startTime", "10:30",
                                    "endTime", "23:00",
                                    "address", Map.of(
                                            "city", "shiraz",
                                            "country", "Iran",
                                            "street", "7th"
                                    )
                            )
                    }
            };
        }

        @ParameterizedTest
        @MethodSource("addRestaurantValidParamsParameters")
        void AddRestaurantShouldAddRestaurantCorrectly_whenParametersAreValid(Map<String, Object> params) throws Exception {
            int restaurantId = 1;
            String name = (String) params.get("name");
            String type = (String) params.get("type");
            String description = (String) params.get("description");
            String image = params.get("image") == null ? ControllerUtils.PLACEHOLDER_IMAGE : (String) params.get("image");
            LocalTime startTime = LocalTime.parse((String) params.get("startTime"), ControllerUtils.TIME_FORMATTER);
            LocalTime endTime = LocalTime.parse((String) params.get("endTime"), ControllerUtils.TIME_FORMATTER);
            Map<String, String> addr = (Map<String, String>) params.get("address");
            address = new Address(addr.get("country"), addr.get("city"), addr.get("street"));

            when(restaurantService.addRestaurant(eq(name), eq(type), eq(startTime), eq(endTime), eq(description),
                    any(Address.class), eq(image))).thenReturn(restaurantId);

            mockMvc.perform(post("/restaurants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(params)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", CoreMatchers.is("restaurant added")))
                    .andExpect(jsonPath("$.success", CoreMatchers.is(true)))
                    .andExpect(jsonPath("$.data").isNumber())
                    .andExpect(jsonPath("$.data", CoreMatchers.is(restaurantId)));
        }

        static Object[][] addRestaurantMissedParameters() {
            return new Object[][]{
                    {
                            Map.of( //no name
                                    "type", "dirtyfood",
                                    "startTime", "08:30",
                                    "endTime", "23:00",
                                    "address", Map.of(
                                            "city", "Tehran",
                                            "country", "Iran",
                                            "street", "23th"
                                    )
                            )
                    },
                    {
                            Map.of( //no type
                                    "name", "Chaman",
                                    "description", "best dirty food in town",
                                    "endTime", "23:00",
                                    "address", Map.of(
                                            "city", "Tehran",
                                            "country", "Iran",
                                            "street", "23th"
                                    )
                            )
                    },
                    {
                            Map.of( //no description
                                    "name", "Chaman",
                                    "type", "dirtyfood",
                                    "startTime", "08:30",
                                    "endTime", "23:00",
                                    "address", Map.of(
                                            "city", "Tehran",
                                            "country", "Iran",
                                            "street", "23th"
                                    )
                            )
                    },
                    {
                            Map.of( //no startTime
                                    "name", "Chaman",
                                    "type", "dirtyfood",
                                    "description", "best dirty food in town",
                                    "endTime", "23:00",
                                    "address", Map.of(
                                            "city", "Tehran",
                                            "country", "Iran",
                                            "street", "23th"
                                    )
                            )
                    },
                    {
                            Map.of( //no endTime
                                    "name", "Chaman",
                                    "type", "dirtyfood",
                                    "description", "best dirty food in town",
                                    "startTime", "08:30",
                                    "address", Map.of(
                                            "city", "Tehran",
                                            "country", "Iran",
                                            "street", "23th"
                                    )
                            )
                    },
                    {
                            Map.of( //no address
                                    "name", "Chaman",
                                    "type", "dirtyfood",
                                    "description", "best dirty food in town",
                                    "startTime", "08:30",
                                    "endTime", "23:00"
                            )
                    },
                    {
                            Map.of(
                                    "name", "Chaman",
                                    "type", "dirtyfood",
                                    "description", "best dirty food in town",
                                    "startTime", "08:30",
                                    "endTime", "23:00",
                                    "address", Map.of(
                                            "city", "Tehran"
                                    )
                            )
                    }
            };
        }

        @ParameterizedTest
        @MethodSource("addRestaurantMissedParameters")
        void AddRestaurantShouldReturnBadRequest_whenParametersIsMissed(Map<String, Object> params) throws Exception {
            mockMvc.perform(post("/restaurants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(params)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", CoreMatchers.is(false)));
        }
        static Object[][] addRestaurantNotExistsParameters() {
            return new Object[][]{
                    {
                            Map.of(
                                    "name", "",
                                    "type", "dirtyfood",
                                    "startTime", "08:30",
                                    "endTime", "23:00",
                                    "address", Map.of(
                                            "city", "Tehran",
                                            "country", "Iran",
                                            "street", "23th"
                                    )
                            )
                    },
                    {
                            Map.of(
                                    "name", "Chaman",
                                    "type", "",
                                    "description", "best dirty food in town",
                                    "endTime", "23:00",
                                    "address", Map.of(
                                            "city", "Tehran",
                                            "country", "Iran",
                                            "street", "23th"
                                    )
                            )
                    },
                    {
                            Map.of( //no description
                                    "name", "Chaman",
                                    "type", "dirtyfood",
                                    "description", "",
                                    "startTime", "08:30",
                                    "endTime", "23:00",
                                    "address", Map.of(
                                            "city", "Tehran",
                                            "country", "Iran",
                                            "street", "23th"
                                    )
                            )
                    },
                    {
                            Map.of(
                                    "name", "Chaman",
                                    "type", "dirtyfood",
                                    "description", "best dirty food in town",
                                    "startTime", "",
                                    "endTime", "23:00",
                                    "address", Map.of(
                                            "city", "Tehran",
                                            "country", "Iran",
                                            "street", "23th"
                                    )
                            )
                    },
                    {
                            Map.of( //no endTime
                                    "name", "Chaman",
                                    "type", "dirtyfood",
                                    "description", "best dirty food in town",
                                    "startTime", "08:30",
                                    "endTime", "",
                                    "address", Map.of(
                                            "city", "Tehran",
                                            "country", "Iran",
                                            "street", "23th"
                                    )
                            )
                    },
                    {
                            Map.of( //no address
                                    "name", "Chaman",
                                    "type", "dirtyfood",
                                    "description", "best dirty food in town",
                                    "startTime", "08:30",
                                    "endTime", "23:00",
                                    "address", Map.of(
                                            "city", "",
                                            "country", "Iran",
                                            "street", "23th"
                                    )
                            )
                    },
                    {
                            Map.of(
                                    "name", "Chaman",
                                    "type", "dirtyfood",
                                    "description", "best dirty food in town",
                                    "startTime", "08:30",
                                    "endTime", "23:00",
                                    "address", Map.of(
                                            "city", "Tehran",
                                            "country", "",
                                            "street", "23th"
                                    )
                            )
                    },
                    {
                            Map.of(
                                    "name", "Chaman",
                                    "type", "dirtyfood",
                                    "description", "best dirty food in town",
                                    "startTime", "08:30",
                                    "endTime", "23:00",
                                    "address", Map.of(
                                            "city", "Tehran",
                                            "country", "Iran",
                                            "street", ""
                                    )
                            )
                    }
            };
        }

        @ParameterizedTest
        @MethodSource("addRestaurantNotExistsParameters")
        void AddRestaurantShouldReturnBadRequest_whenParametersNotExists(Map<String, Object> params) throws Exception {
            mockMvc.perform(post("/restaurants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(params)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", CoreMatchers.is(false)));
        }

        private Map<String, Object> generateRestaurantParams() {
            Map<String, Object> params = Map.of(
                    "name", "chaman",
                    "type", "dirtyfood",
                    "description", "best dirty food in town",
                    "startTime", "08:30",
                    "endTime", "23:00",
                    "address", Map.of(
                            "city", "Tehran",
                            "country", "Iran",
                            "street", "3th"
                    )
            );
            return params;
        }

        static Object[][] addRestaurantInvalidParameters() {
            return new Object[][]{
                    {DuplicatedRestaurantName.class},
                    {UserNotManager.class},
                    {InvalidWorkingTime.class},
            };
        }

        @ParameterizedTest
        @MethodSource("addRestaurantInvalidParameters")
        void AddRestaurantShouldReturnBadRequest_whenParametersAreInvalid(Class<? extends Throwable> exception) throws Exception {
            when(restaurantService.addRestaurant(any(String.class), any(String.class), any(LocalTime.class),
                    any(LocalTime.class), any(String.class), any(Address.class), any(String.class))).thenThrow(exception);
            Map<String, Object> params = generateRestaurantParams();
            mockMvc.perform(post("/restaurants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(params)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", CoreMatchers.is(false)));
        }
    }

    @Nested
    class ValidateRestaurantName {

        @Test
        void ValidateNameShouldReturnConflictResponse_whenRestaurantWithChosenNameIsExisted() throws Exception {
            String name = "Chaman";
            when(restaurantService.restaurantExists(eq(name))).thenReturn(true);

            mockMvc.perform(get("/validate/restaurant-name")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("data", name))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message", CoreMatchers.is("restaurant name is taken")))
                    .andExpect(jsonPath("$.success", CoreMatchers.is(false)));
        }

        @Test
        void shouldReturnOkResponse_whenRestaurantWithChosenNameIsNotExisted() throws Exception {
            String name = "Chaman";
            when(restaurantService.restaurantExists(eq(name))).thenReturn(false);

            mockMvc.perform(get("/validate/restaurant-name")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("data", name))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", CoreMatchers.is("restaurant name is available")))
                    .andExpect(jsonPath("$.success", CoreMatchers.is(true)));
        }
    }

    @Nested
    class GetRestaurantTypes {

        @Test
        void GetRestaurantTypesShouldReturnRestaurantTypes() throws Exception {
            Set<String> types = Set.of("Italian", "Chinese", "Persian", "Fastfood");
            when(restaurantService.getRestaurantTypes()).thenReturn(types);

            mockMvc.perform(get("/restaurants/types")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", CoreMatchers.is(true)))
                    .andExpect(jsonPath("$.message", CoreMatchers.is("restaurant types")))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", containsInAnyOrder(types.toArray())));
        }

        @Test
        public void GetRestaurantTypesShouldReturnBadRequest_whenExceptionIsThrown() throws Exception {
            when(restaurantService.getRestaurantTypes()).thenThrow(new RuntimeException("Error"));

            mockMvc.perform(get("/restaurants/types")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", CoreMatchers.is(false)))
                    .andExpect(jsonPath("$.message", CoreMatchers.is("Error")));
        }
    }

    @Nested
    class GetRestaurantLocations {

        @Test
        void shouldReturnRestaurantLocations() throws Exception {
            Map<String, Set<String>> locations = Map.of(
                    "Tehran", Set.of("Burger Land", "Nayeb"),
                    "Shiraz", Set.of("Chaman")
            );

            when(restaurantService.getRestaurantLocations()).thenReturn(locations);

            mockMvc.perform(get("/restaurants/locations")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", CoreMatchers.is(true)))
                    .andExpect(jsonPath("$.message", CoreMatchers.is("restaurant locations")))
                    .andExpect(jsonPath("$.data").isMap())
                    .andExpect(jsonPath("$.data.Tehran", containsInAnyOrder(locations.get("Tehran").toArray())))
                    .andExpect(jsonPath("$.data.Shiraz", containsInAnyOrder(locations.get("Shiraz").toArray())))
                    .andDo(print());
        }

        @Test
        void shouldReturnBadRequest_whenExceptionIsThrown() throws Exception {
            when(restaurantService.getRestaurantLocations()).thenThrow(new RuntimeException("Error"));

            mockMvc.perform(get("/restaurants/locations")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", CoreMatchers.is(false)))
                    .andExpect(jsonPath("$.message", CoreMatchers.is("Error")));
        }
    }
}

