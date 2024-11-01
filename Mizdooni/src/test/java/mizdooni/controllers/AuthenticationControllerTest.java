package mizdooni.controllers;

import mizdooni.exceptions.DuplicatedUsernameEmail;
import mizdooni.exceptions.InvalidEmailFormat;
import mizdooni.exceptions.InvalidUsernameFormat;
import mizdooni.model.Address;
import mizdooni.model.User;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.ServiceUtils;
import mizdooni.service.UserService;
import org.junit.jupiter.api.AfterEach;
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
import static mizdooni.controllers.ControllerUtils.PARAMS_BAD_TYPE;
import static mizdooni.controllers.ControllerUtils.PARAMS_MISSING;

class AuthenticationControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthenticationController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

//    @AfterEach
//    void tearDown() {
//        reset(userService);
//    }

    @Test
    void testGetCurrentUser_UserLoggedIn() {
        User mockUser = new User("testuser", "password", "test@email.com", 
            new Address("Country", "City", null), User.Role.client);
        
        when(userService.getCurrentUser()).thenReturn(mockUser);

        Response response = authController.user();
        
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(mockUser, response.getData());
        assertEquals("current user", response.getMessage());
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
            new Address("Country", "City", null), User.Role.client);

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
    void testLogin_MissingParams() {
        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("username", "testuser");

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.login(loginParams);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("parameters missing", exception.getMessage());
    }

    @Test
    void testSignup_Successful() throws DuplicatedUsernameEmail, InvalidUsernameFormat, InvalidEmailFormat {
        Map<String, Object> signupParams = new HashMap<>();
        signupParams.put("username", "newuser");
        signupParams.put("password", "password");
        signupParams.put("email", "new@email.com");
        signupParams.put("role", "client");

        Map<String, String> addressParams = new HashMap<>();
        addressParams.put("country", "TestCountry");
        addressParams.put("city", "TestCity");
        signupParams.put("address", addressParams);

        User mockUser = new User("newuser", "password", "new@email.com",
            new Address("TestCountry", "TestCity", null), User.Role.client);

        doNothing().when(userService).signup(
            eq("newuser"), eq("password"), eq("new@email.com"),
            any(Address.class), eq(User.Role.client)
        );
        when(userService.login("newuser", "password")).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(mockUser);

        Response response = authController.signup(signupParams);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(mockUser, response.getData());
        verify(userService).signup(
            eq("newuser"), eq("password"), eq("new@email.com"),
            any(Address.class), eq(User.Role.client)
        );
        verify(userService).login("newuser", "password");
    }

    @Test
    void testSignup_MissingParams() {
        Map<String, Object> signupParams = new HashMap<>();
        signupParams.put("username", "newuser");
        signupParams.put("password", "password");
        signupParams.put("email", "");

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.signup(signupParams);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_MISSING, exception.getMessage());
    };

    @Test
    void testSignup_InvalidParams() {
        Map<String, Object> signupParams = new HashMap<>();
        signupParams.put("username", "newuser");
        signupParams.put("password", "password");
        signupParams.put("email", "email.com");
        signupParams.put("role", "invalidrole");

        Map<String, String> addressParams = new HashMap<>();
        addressParams.put("country", "TestCountry");
        addressParams.put("city", "TestCity");
        signupParams.put("address", addressParams);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.signup(signupParams);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_BAD_TYPE, exception.getMessage());
    }

    @Test
    void testSignup_EmptyParams() {
        Map<String, Object> signupParams = new HashMap<>();
        signupParams.put("username", "");
        signupParams.put("password", "password");
        signupParams.put("email", "email.com");
        signupParams.put("role", "client");

        Map<String, String> addressParams = new HashMap<>();
        addressParams.put("country", "TestCountry");
        addressParams.put("city", "TestCity");
        signupParams.put("address", addressParams);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.signup(signupParams);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_MISSING, exception.getMessage());
    }

    @Test
    void testSignup_DuplicatedUsernameEmail() throws DuplicatedUsernameEmail, InvalidUsernameFormat, InvalidEmailFormat {
        Map<String, Object> signupParams = new HashMap<>();
        signupParams.put("username", "newuser");
        signupParams.put("password", "password");
        signupParams.put("email", "new@email.com");
        signupParams.put("role", "client");

        Map<String, String> addressParams = new HashMap<>();
        addressParams.put("country", "TestCountry");
        addressParams.put("city", "TestCity");
        signupParams.put("address", addressParams);

        User mockUser = new User("newuser", "password", "new@email.com",
                new Address("TestCountry", "TestCity", null), User.Role.client);

        doThrow(new DuplicatedUsernameEmail()).when(userService).signup(
                eq("newuser"), eq("password"), eq("new@email.com"),
                any(Address.class), eq(User.Role.client)
        );

        when(userService.login("newuser", "password")).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(mockUser);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.signup(signupParams);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    };

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
        String username = "validuser";
        when(userService.usernameExists(username)).thenReturn(false);

        Response response = authController.validateUsername(username);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("username is available", response.getMessage());
    }

    @Test
    void testValidateUsername_InvalidFormat() {
        String username = "inv@lid";

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.validateUsername(username);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("invalid username format", exception.getMessage());
    }

    @Test
    void testValidateUsername_AlreadyExists() {
        String username = "existinguser";
        when(userService.usernameExists(username)).thenReturn(true);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.validateUsername(username);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("username already exists", exception.getMessage());
    }

    @Test
    void testValidateEmail_Available() {
        String email = "valid@email.com";
        when(userService.emailExists(email)).thenReturn(false);

        Response response = authController.validateEmail(email);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("email not registered", response.getMessage());
    }

    @Test
    void testValidateEmail_InvalidFormat() {
        String email = "invalidemail.com";

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.validateEmail(email);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("invalid email format", exception.getMessage());
    }

    @Test
    void testValidateEmail_AlreadyExists() {
        String email = "exists@email.com";
        when(userService.emailExists(email)).thenReturn(true);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            authController.validateEmail(email);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("email already registered", exception.getMessage());
    };
}
