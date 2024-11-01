package mizdooni.controllers;

import mizdooni.exceptions.*;
import mizdooni.model.*;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.ReservationService;
import mizdooni.service.RestaurantService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static mizdooni.controllers.ControllerUtils.*;

class ReservationControllerTest {

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    User manager;
    User client;
    Address address;
    Restaurant restaurant;

    List<Reservation> Reservations;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        manager = new User("Bardia", "123", "bardia@gmail.com", new Address("Iran", "Tehran", "1st street"), User.Role.manager);
        client = new User("Ali", "123", "ali@gmail.com,", new Address("Iran", "Tehran", "3rd street"), User.Role.client);
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
        Table table1 = new Table(2, restaurant.getId(), 5);
        Table table2 = new Table(2, restaurant.getId(), 4);

        Reservations = Arrays.asList(
                new Reservation(client,restaurant,table1, LocalDateTime.now()),
                new Reservation(client, restaurant, table2, LocalDateTime.now())
        );
    }

    @AfterEach
    void tearDown() {
        manager = null;
        client = null;
        address = null;
        restaurant = null;
    }

    @Test
    void testGetReservations_Successful() throws UserNotManager, TableNotFound, InvalidManagerRestaurant, RestaurantNotFound {
        int tableNumber = 2;
        LocalDate date = LocalDate.parse("2024-01-15", DATE_FORMATTER);


        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);

        when(reservationService.getReservations(restaurant.getId(), tableNumber, date))
            .thenReturn(Reservations);

        Response response = reservationController.getReservations(restaurant.getId(), tableNumber, "2024-01-15");

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(Reservations, response.getData());
        verify(restaurantService).getRestaurant(restaurant.getId());
        verify(reservationService).getReservations(restaurant.getId(), tableNumber, date);
    }

    @Test
    void testGetReservations_RestaurantNotFound()
    {
        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(null);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reservationController.getReservations(restaurant.getId(), 2, "2024-01-15");
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("restaurant not found", exception.getMessage());
    };

    @Test
    void testGetReservations_WrongDateFormat()
    {
        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reservationController.getReservations(restaurant.getId(), 2, "2024-01-15 19:00");
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_BAD_TYPE, exception.getMessage());
    };

    @Test
    void testGetReservations_TableNotFound() throws UserNotManager, InvalidManagerRestaurant, RestaurantNotFound, TableNotFound {
        int tableNumber = 2;
        LocalDate date = LocalDate.parse("2024-01-15", DATE_FORMATTER);

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);

        when(reservationService.getReservations(restaurant.getId(), tableNumber, date))
            .thenThrow(new TableNotFound());

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reservationController.getReservations(restaurant.getId(), tableNumber, "2024-01-15");
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Table not found.", exception.getMessage());
        verify(restaurantService).getRestaurant(restaurant.getId());
        verify(reservationService).getReservations(restaurant.getId(), tableNumber, date);
    }

    @Test
    void testGetCustomerReservations_Successful() throws UserNotFound, UserNoAccess {
        int customerId = 1;

        when(reservationService.getCustomerReservations(customerId))
            .thenReturn(Reservations);

        Response response = reservationController.getCustomerReservations(customerId);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(Reservations, response.getData());
        verify(reservationService).getCustomerReservations(customerId);
    }

    @Test
    void testGetCustomerReservations_UserNotFound() throws UserNotFound, UserNoAccess {
        int customerId = 1;

        when(reservationService.getCustomerReservations(customerId))
            .thenThrow(new UserNotFound());

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reservationController.getCustomerReservations(customerId);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("User not found.", exception.getMessage());
        verify(reservationService).getCustomerReservations(customerId);
    }

    @Test
    void testGetAvailableTimes_Successful() throws DateTimeInThePast, RestaurantNotFound, BadPeopleNumber {
        int people = 4;
        LocalDate date = LocalDate.parse("2024-01-15", DATE_FORMATTER);

        List<LocalTime> mockAvailableTimes = Arrays.asList(
            LocalTime.of(12, 0),
            LocalTime.of(14, 0)
        );

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);
        when(reservationService.getAvailableTimes(restaurant.getId(), people, date))
            .thenReturn(mockAvailableTimes);

        Response response = reservationController.getAvailableTimes(restaurant.getId(), people, "2024-01-15");

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(mockAvailableTimes, response.getData());
        verify(restaurantService).getRestaurant(restaurant.getId());
        verify(reservationService).getAvailableTimes(restaurant.getId(), people, date);
    }

    @Test
    void testGetAvailableTimes_RestaurantNotFound() throws DateTimeInThePast, RestaurantNotFound, BadPeopleNumber {
        int people = 4;
        LocalDate date = LocalDate.parse("2024-01-15", DATE_FORMATTER);

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(null);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reservationController.getAvailableTimes(restaurant.getId(), people, "2024-01-15");
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("restaurant not found", exception.getMessage());
        verify(restaurantService).getRestaurant(restaurant.getId());
    }

    @Test
    void testGetAvailableTimes_DateTimeInThePast() throws DateTimeInThePast, RestaurantNotFound, BadPeopleNumber {
        int people = 4;
        LocalDate date = LocalDate.parse("2022-01-15", DATE_FORMATTER);

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);
        when(reservationService.getAvailableTimes(restaurant.getId(), people, date))
            .thenThrow(new DateTimeInThePast());

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reservationController.getAvailableTimes(restaurant.getId(), people, "2022-01-15");
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Date time is before current time.", exception.getMessage());
        verify(restaurantService).getRestaurant(restaurant.getId());
        verify(reservationService).getAvailableTimes(restaurant.getId(), people, date);
    }

    @Test
    void testAddReservation_Successful() throws UserNotFound, DateTimeInThePast, TableNotFound, ReservationNotInOpenTimes, ManagerReservationNotAllowed, RestaurantNotFound, InvalidWorkingTime {
        Map<String, String> params = new HashMap<>();
        params.put("people", "4");
        params.put("datetime", "2024-01-15 19:00");

        Reservation reservation = new Reservation(client, restaurant, new Table(2, restaurant.getId(), 4), LocalDateTime.parse("2024-01-15 19:00", DATETIME_FORMATTER));

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);
        when(reservationService.reserveTable(
            eq(restaurant.getId()),
            eq(4),
            eq(LocalDateTime.parse("2024-01-15 19:00", DATETIME_FORMATTER))
        )).thenReturn(reservation);

        Response response = reservationController.addReservation(restaurant.getId(), params);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(reservation, response.getData());
        verify(restaurantService).getRestaurant(restaurant.getId());
        verify(reservationService).reserveTable(
            eq(restaurant.getId()),
            eq(4),
            eq(LocalDateTime.parse("2024-01-15 19:00", DATETIME_FORMATTER))
        );
    }

    @Test
    void testAddReservation_RestaurantNotFound() throws UserNotFound, DateTimeInThePast, TableNotFound, ReservationNotInOpenTimes, ManagerReservationNotAllowed, RestaurantNotFound, InvalidWorkingTime {
        Map<String, String> params = new HashMap<>();
        params.put("people", "4");
        params.put("datetime", "2024-01-15 19:00");

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(null);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reservationController.addReservation(restaurant.getId(), params);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("restaurant not found", exception.getMessage());
        verify(restaurantService).getRestaurant(restaurant.getId());
    }

    @Test
    void testAddReservation_InvalidParams()
    {
        Map<String, String> params = new HashMap<>();
        params.put("people", "4");

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reservationController.addReservation(restaurant.getId(), params);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_MISSING, exception.getMessage());
    };

    @Test
    void testAddReservation_BadParamType()
    {
        Map<String, String> params = new HashMap<>();
        params.put("people", "four");
        params.put("datetime", "2024-01-15 19:00");

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reservationController.addReservation(restaurant.getId(), params);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_BAD_TYPE, exception.getMessage());
    };

    @Test
    void testAddReservation_TableNotFound() throws UserNotFound, DateTimeInThePast, TableNotFound, ReservationNotInOpenTimes, ManagerReservationNotAllowed, RestaurantNotFound, InvalidWorkingTime {
        Map<String, String> params = new HashMap<>();
        params.put("people", "4");
        params.put("datetime", "2024-01-15 19:00");

        when(restaurantService.getRestaurant(restaurant.getId())).thenReturn(restaurant);
        when(reservationService.reserveTable(
            eq(restaurant.getId()),
            eq(4),
            eq(LocalDateTime.parse("2024-01-15 19:00", DATETIME_FORMATTER))
        )).thenThrow(new TableNotFound());

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reservationController.addReservation(restaurant.getId(), params);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Table not found.", exception.getMessage());
        verify(restaurantService).getRestaurant(restaurant.getId());
        verify(reservationService).reserveTable(
            eq(restaurant.getId()),
            eq(4),
            eq(LocalDateTime.parse("2024-01-15 19:00", DATETIME_FORMATTER))
        );
    }

    @Test
    void testCancelReservation_Successful() throws ReservationCannotBeCancelled, UserNotFound, ReservationNotFound {
        int reservationNumber = 123;

        doNothing().when(reservationService).cancelReservation(reservationNumber);

        Response response = reservationController.cancelReservation(reservationNumber);

        assertEquals(HttpStatus.OK, response.getStatus());
        verify(reservationService).cancelReservation(reservationNumber);
    }

    @Test
    void testCancelReservation_UserNotFound() throws ReservationCannotBeCancelled, UserNotFound, ReservationNotFound {
        int reservationNumber = 123;

        doThrow(new UserNotFound()).when(reservationService).cancelReservation(reservationNumber);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reservationController.cancelReservation(reservationNumber);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("User not found.", exception.getMessage());
        verify(reservationService).cancelReservation(reservationNumber);
    }
}
