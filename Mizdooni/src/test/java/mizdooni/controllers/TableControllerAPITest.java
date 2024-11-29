package mizdooni.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import mizdooni.model.Address;
import mizdooni.model.User;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;


import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import mizdooni.exceptions.UserNotManager;
import mizdooni.exceptions.InvalidManagerRestaurant;
import mizdooni.model.Restaurant;
import mizdooni.model.Table;
import mizdooni.service.RestaurantService;
import mizdooni.service.TableService;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) //disable all filters and security
public class TableControllerAPITest {

    @MockBean
    private RestaurantService restaurantService;
    @MockBean
    private TableService tableService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private static User manager;

    @MockBean
    private static Address address;
    private Table table;

//    @BeforeEach
//    void setUp()
//    {
//        manager = new User("Bardia", "123", "bardia@gmail.com", new Address("Iran", "Tehran", "1st street"), User.Role.manager);
//        address = new Address("Iran", "Tehran", "2nd street");
//    }
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

    private static Table CreateTable(Restaurant restaurant) {
        Table table = new Table(1, restaurant.getId(), 4);
        return table;
    }

    private static List<Table> CreateTables(Restaurant restaurant) {
        List<Table> tables = new ArrayList<>();
        tables.add(CreateTable(restaurant));
        return tables;
    }

    @Nested
    class GetTables {
        static Object[][] getTableParameters() {
            Restaurant restaurant = CreateRestaurant();
            return new Object[][] {
                    {
                            restaurant,
                            CreateTables(restaurant)
                    },
                    {
                            restaurant,
                            Collections.emptyList()
                    }
            };
        }
        @ParameterizedTest
        @MethodSource("getTableParameters")
        void getTablesShouldReturnRestaurantTables_WhenRestaurantIdIsValid(Restaurant restaurant, List<Table> tables) throws Exception{
            int restaurantId = restaurant.getId();
            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            assertEquals(restaurant, restaurantService.getRestaurant(restaurantId));
            when(tableService.getTables(restaurantId)).thenReturn(tables);
            assertEquals(tables, tableService.getTables(restaurantId));
            assertEquals(restaurantId, restaurant.getId());

            ResultActions response = mockMvc.perform(get("/tables/{restaurantId}", Integer.toString(restaurantId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", CoreMatchers.is("tables listed")))
                    .andExpect(jsonPath("$.success", CoreMatchers.is(true)))
                    .andExpect(jsonPath("$.data").isArray());

            for (int i = 0; i < tables.size(); i++) {
                String basePath = "$.data[" + i + "]";
                response.andExpect(jsonPath(basePath + "tableNumber", CoreMatchers.is(tables.get(i).getTableNumber())))
                        .andExpect(jsonPath(basePath + "seatsNumber", CoreMatchers.is(tables.get(i).getSeatsNumber())));
            }
        }

        @Test
        void getTablesShouldReturnNotFound_WhenRestaurantIdIsNotExisted() throws Exception {
            int restaurantId = -1;
            when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);
            mockMvc.perform(get("/tables/{restaurantId}", Integer.toString(restaurantId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", CoreMatchers.is(false)));
        }

        @Test
        void getTablesShouldReturnBadRequest_whenRestaurantIdIsInvalidFormat() throws Exception {
            mockMvc.perform(get("/tables/{restaurantId}", "invalid id")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class AddTable {

        @Test
        void AddTableShouldAddTable_whenParametersAreOkAndRestaurantExists() throws Exception {
            Restaurant restaurant = CreateRestaurant();
            int restaurantId = restaurant.getId();
            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            int seatsNumber = 4;
            doNothing().when(tableService).addTable(restaurantId, seatsNumber);

            mockMvc.perform(post("/tables/{restaurantId}", Integer.toString(restaurantId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("seatsNumber", Integer.toString(seatsNumber)))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", CoreMatchers.is(true)))
                    .andExpect(jsonPath("$.message", CoreMatchers.is("table added")));
        }

        @Test
        void AddTableShouldReturnNotFound_whenRestaurantIdIsNotExisted() throws Exception {
            int restaurantId = -1;
            int seats_number = 4;
            when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);
            mockMvc.perform(get("/tables/{restaurantId}", Integer.toString(restaurantId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("seatsNumber", Integer.toString(seats_number)))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", CoreMatchers.is(false)));
        }

        @Test
        void AddTablesShouldReturnBadRequest_whenRestaurantIdIsInvalidFormat() throws Exception {
            int seats_number = 4;
            mockMvc.perform(get("/tables/{restaurantId}", "invalid id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("seatsNumber", Integer.toString(seats_number))))
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        void AddTableShouldReturnBadRequest_whenSeatsNumberIsMissed() throws Exception {
            Restaurant restaurant = CreateRestaurant();
            int restaurantId = restaurant.getId();
            int seatsNumber = 4;
            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);

            mockMvc.perform(post("/tables/{restaurantId}", Integer.toString(restaurantId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("seats", Integer.toString(seatsNumber)))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", CoreMatchers.is(false)));
        }

        static Object[][] addTableInvalidSeatsNumberFormatParameters() {
            return new Object[][] {
                    { "" },
                    { "invalid seats number" }
            };
        }
        @ParameterizedTest
        @MethodSource("addTableInvalidSeatsNumberFormatParameters")
        void AddTableShouldReturnBadRequest_whenSeatsNumberIsInvalidFormat(Object seatsNumber) throws Exception {
            Restaurant restaurant = CreateRestaurant();
            int restaurantId = restaurant.getId();
            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);

            mockMvc.perform(post("/tables/{restaurantId}", restaurantId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("seatsNumber", seatsNumber))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", CoreMatchers.is(false)));
        }

        static Object[][] addTableInvalidSeatsNumberParameters() {
            return new Object[][] {
                    { 0 },
                    { -1 }
            };
        }
        @ParameterizedTest
        @MethodSource("addTableInvalidSeatsNumberParameters")
        void AddTableShouldReturnBadRequest_whenSeatsNumberIsZeroOrNegative(int seatsNumber) throws Exception {
            Restaurant restaurant = CreateRestaurant();
            int restaurantId = restaurant.getId();
            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);

            mockMvc.perform(post("/tables/{restaurantId}", restaurantId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("seatsNumber", seatsNumber)))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", CoreMatchers.is(false)));
        }

        static Object[][] addTableExceptionRaisedParameters() {
            return new Object[][] {
                    { UserNotManager.class },
                    { InvalidManagerRestaurant.class }
            };
        }
        @ParameterizedTest
        @MethodSource("addTableExceptionRaisedParameters")
        void shouldReturnBadRequest_whenExceptionIsRaised(Class<? extends Throwable> exception) throws Exception {
            Restaurant restaurant = CreateRestaurant();
            int restaurantId = restaurant.getId();
            when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
            int seatsNumber = 2;
            doThrow(exception).when(tableService).addTable(restaurantId, seatsNumber);

            mockMvc.perform(post("/tables/{restaurantId}", Integer.toString(restaurantId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("seatsNumber", Integer.toString(seatsNumber)))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", CoreMatchers.is(false)))
                    .andDo(print());
        }
    }
}