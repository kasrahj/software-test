package mizdooni.controllers;

import mizdooni.model.Address;
import mizdooni.model.User;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.ServiceUtils;
import mizdooni.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthenticationController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCurrentUser_UserLoggedIn() {
        User mockUser = new User("testuser", "password", "test@email.com", 
            new Address("Country", "City", null), User.Role.CUSTOMER);
        
        when(userService.getCurrentUser()).thenReturn(mockUser);

        Response response = authController.user();
        
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(mockUser, response.getData());
    }

    @Test
    void testGetCurrentUser_NoUserLoggedIn() {
        when(userService.getCurrentUser()).thenReturn(null);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.user();
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("no user logged in", exception.getMessage());
    }

    @Test
    void testLogin_Successful() {
        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("username", "testuser");
        loginParams.put("password", "password");

        User mockUser = new User("testuser", "password", "test@email.com", 
            new Address("Country", "City", null), User.Role.CUSTOMER);

        when(userService.login("testuser", "password")).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(mockUser);

        Response response = authController.login(loginParams);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(mockUser, response.getData());
        verify(userService).login("testuser", "password");
    }

    @Test
    void testLogin_InvalidCredentials() {
        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("username", "testuser");
        loginParams.put("password", "wrongpassword");

        when(userService.login("testuser", "wrongpassword")).thenReturn(false);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.login(loginParams);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("invalid username or password", exception.getMessage());
    }

    @Test
    void testSignup_Successful() {
        Map<String, Object> signupParams = new HashMap<>();
        signupParams.put("username", "newuser");
        signupParams.put("password", "password");
        signupParams.put("email", "new@email.com");
        signupParams.put("role", "CUSTOMER");

        Map<String, String> addressParams = new HashMap<>();
        addressParams.put("country", "TestCountry");
        addressParams.put("city", "TestCity");
        signupParams.put("address", addressParams);

        User mockUser = new User("newuser", "password", "new@email.com", 
            new Address("TestCountry", "TestCity", null), User.Role.CUSTOMER);

        doNothing().when(userService).signup(
            eq("newuser"), eq("password"), eq("new@email.com"), 
            any(Address.class), eq(User.Role.CUSTOMER)
        );
        when(userService.login("newuser", "password")).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(mockUser);

        Response response = authController.signup(signupParams);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(mockUser, response.getData());
        verify(userService).signup(
            eq("newuser"), eq("password"), eq("new@email.com"), 
            any(Address.class), eq(User.Role.CUSTOMER)
        );
        verify(userService).login("newuser", "password");
    }

    @Test
    void testLogout_Successful() {
        when(userService.logout()).thenReturn(true);

        Response response = authController.logout();

        assertEquals(HttpStatus.OK, response.getStatus());
        verify(userService).logout();
    }

    @Test
    void testLogout_NoUserLoggedIn() {
        when(userService.logout()).thenReturn(false);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.logout();
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("no user logged in", exception.getMessage());
    }

    @Test
    void testValidateUsername_Available() {
        when(ServiceUtils.validateUsername("validuser")).thenReturn(true);
        when(userService.usernameExists("validuser")).thenReturn(false);

        Response response = authController.validateUsername("validuser");

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("username is available", response.getMessage());
    }

    @Test
    void testValidateUsername_InvalidFormat() {
        when(ServiceUtils.validateUsername("inv@lid")).thenReturn(false);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.validateUsername("inv@lid");
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("invalid username format", exception.getMessage());
    }

    @Test
    void testValidateUsername_AlreadyExists() {
        when(ServiceUtils.validateUsername("existinguser")).thenReturn(true);
        when(userService.usernameExists("existinguser")).thenReturn(true);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.validateUsername("existinguser");
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("username already exists", exception.getMessage());
    }

    @Test
    void testValidateEmail_Available() {
        when(ServiceUtils.validateEmail("valid@email.com")).thenReturn(true);
        when(userService.emailExists("valid@email.com")).thenReturn(false);

        Response response = authController.validateEmail("valid@email.com");

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("email not registered", response.getMessage());
    }
}
