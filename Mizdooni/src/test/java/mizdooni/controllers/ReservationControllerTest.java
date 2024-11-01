package mizdooni.controllers;

import mizdooni.model.Reservation;
import mizdooni.model.Restaurant;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.ReservationService;
import mizdooni.service.RestaurantService;
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

class ReservationControllerTest {

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetReservations_Successful() {
        int restaurantId = 1;
        int tableNumber = 2;
        LocalDate date = LocalDate.parse("2024-01-15", DATE_FORMATTER);

        Restaurant mockRestaurant = new Restaurant();
        mockRestaurant.setId(restaurantId);

        List<Reservation> mockReservations = Arrays.asList(
            new Reservation(), new Reservation()
        );

        when(restaurantService.findById(restaurantId)).thenReturn(mockRestaurant);
        when(reservationService.getReservations(restaurantId, tableNumber, date))
            .thenReturn(mockReservations);

        Response response = reservationController.getReservations(restaurantId, tableNumber, "2024-01-15");

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(mockReservations, response.getData());
        verify(restaurantService).findById(restaurantId);
        verify(reservationService).getReservations(restaurantId, tableNumber, date);
    }

    @Test
    void testGetCustomerReservations_Successful() {
        int customerId = 1;
        List<Reservation> mockReservations = Arrays.asList(
            new Reservation(), new Reservation()
        );

        when(reservationService.getCustomerReservations(customerId))
            .thenReturn(mockReservations);

        Response response = reservationController.getCustomerReservations(customerId);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(mockReservations, response.getData());
        verify(reservationService).getCustomerReservations(customerId);
    }

    @Test
    void testGetAvailableTimes_Successful() {
        int restaurantId = 1;
        int people = 4;
        LocalDate date = LocalDate.parse("2024-01-15", DATE_FORMATTER);

        Restaurant mockRestaurant = new Restaurant();
        mockRestaurant.setId(restaurantId);

        List<LocalTime> mockAvailableTimes = Arrays.asList(
            LocalTime.of(12, 0),
            LocalTime.of(14, 0)
        );

        when(restaurantService.findById(restaurantId)).thenReturn(mockRestaurant);
        when(reservationService.getAvailableTimes(restaurantId, people, date))
            .thenReturn(mockAvailableTimes);

        Response response = reservationController.getAvailableTimes(restaurantId, people, "2024-01-15");

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(mockAvailableTimes, response.getData());
        verify(restaurantService).findById(restaurantId);
        verify(reservationService).getAvailableTimes(restaurantId, people, date);
    }

    @Test
    void testAddReservation_Successful() {
        int restaurantId = 1;
        Map<String, String> params = new HashMap<>();
        params.put("people", "4");
        params.put("datetime", "2024-01-15 19:00");

        Restaurant mockRestaurant = new Restaurant();
        mockRestaurant.setId(restaurantId);

        Reservation mockReservation = new Reservation();
        mockReservation.setRestaurantId(restaurantId);

        when(restaurantService.findById(restaurantId)).thenReturn(mockRestaurant);
        when(reservationService.reserveTable(
            eq(restaurantId),
            eq(4),
            eq(LocalDateTime.parse("2024-01-15 19:00", DATETIME_FORMATTER))
        )).thenReturn(mockReservation);

        Response response = reservationController.addReservation(restaurantId, params);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(mockReservation, response.getData());
        verify(restaurantService).findById(restaurantId);
        verify(reservationService).reserveTable(
            eq(restaurantId),
            eq(4),
            eq(LocalDateTime.parse("2024-01-15 19:00", DATETIME_FORMATTER))
        );
    }

    @Test
    void testCancelReservation_Successful() {
        int reservationNumber = 123;

        doNothing().when(reservationService).cancelReservation(reservationNumber);

        Response response = reservationController.cancelReservation(reservationNumber);

        assertEquals(HttpStatus.OK, response.getStatus());
        verify(reservationService).cancelReservation(reservationNumber);
    }
}
