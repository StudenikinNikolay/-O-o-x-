package edu.diploma;

import edu.diploma.auth.JwtHelper;
import edu.diploma.auth.UserCreds;
import edu.diploma.model.*;
import edu.diploma.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

public class AuthControllerTest {

    private JwtHelper jwtHelper;
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;

    private AuthController authController;

    @BeforeEach
    public void setUp() {

    }

    @Test
    public void testPostLogin400WhenCreentialsNull() {

        UserCreds creds = null;

        authController = new AuthController(jwtHelper,userRepository,passwordEncoder);

        ResponseEntity<?> response = authController.postLogin(creds);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(((LoginErrors) response.getBody() ).getEmail().size(), is(1) );
        assertThat(
                ((LoginErrors) response.getBody()).getEmail().get(0),
                is("Неправильные учетные данные")
        );
    }

    @Test
    public void testPostLogin400WhenLoginBlank() {
        final String username = "   \t \n ";
        final String password = "123pwd";

        UserCreds creds = new UserCreds(username,password);

        authController = new AuthController(jwtHelper,userRepository,passwordEncoder);

        ResponseEntity<?> response = authController.postLogin(creds);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(((LoginErrors) response.getBody() ).getEmail().size(), is(1) );
        assertThat(
                ((LoginErrors) response.getBody()).getEmail().get(0),
                is("Необходимо ввести почту")
        );
    }

    @Test
    public void testPostLogin400WhenLoginNull() {
        final String username = null;
        final String password = "123pwd";
        UserCreds creds = new UserCreds(username,password);

        authController = new AuthController(jwtHelper,userRepository,passwordEncoder);

        ResponseEntity<?> response = authController.postLogin(creds);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(((LoginErrors) response.getBody() ).getEmail().size(), is(1) );
        assertThat(
                ((LoginErrors) response.getBody()).getEmail().get(0),
                is("Необходимо ввести почту")
        );
    }

    @Test
    public void testPostLogin400WhenPasswordBlank() {
        final String username = "user1";
        final String password = " \t \n ";
        UserCreds creds = new UserCreds(username,password);

        authController = new AuthController(jwtHelper,userRepository,passwordEncoder);

        ResponseEntity<?> response = authController.postLogin(creds);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(((LoginErrors) response.getBody() ).getPassword().size(), is(1) );
        assertThat(
                ((LoginErrors) response.getBody()).getPassword().get(0),
                is("Необходимо ввести пароль")
        );
    }

    @Test
    public void testPostLogin400WhenPasswordNull() {
        final String username = "user1";
        final String password = null;
        UserCreds creds = new UserCreds(username,password);

        authController = new AuthController(jwtHelper,userRepository,passwordEncoder);

        ResponseEntity<?> response = authController.postLogin(creds);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(((LoginErrors) response.getBody() ).getPassword().size(), is(1) );
        assertThat(
                ((LoginErrors) response.getBody()).getPassword().get(0),
                is("Необходимо ввести пароль")
        );
    }

    @Test
    public void testPostLogin400NoSuchLogin() {
        final String username = "user1";
        final String password = "123pwd";
        UserCreds creds = new UserCreds(username,password);

        userRepository = mock(UserRepository.class);
        when(userRepository.findOne(any(Example.class))).thenReturn(Optional.empty());

        authController = new AuthController(jwtHelper,userRepository,passwordEncoder);

        ResponseEntity<?> response = authController.postLogin(creds);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(((LoginErrors) response.getBody() ).getEmail().size(), is(1) );
        assertThat(
                ((LoginErrors) response.getBody()).getEmail().get(0),
                is("Неправильно указана почта")
        );
    }

    @Test
    public void testPostLogin400PasswordMismatch() {
        final String username = "user1";
        final String password = "123pwd";
        UserCreds creds = new UserCreds(username,password);

        final String token = "abcd1212x.zLKL.t789Bgre";

        final User user = new User(1L,username, password);

        passwordEncoder = mock(BCryptPasswordEncoder.class);
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);

        userRepository = mock(UserRepository.class);
        when(userRepository.findOne(any(Example.class))).thenReturn(Optional.of(user));

        authController = new AuthController(jwtHelper,userRepository,passwordEncoder);

        ResponseEntity<?> response = authController.postLogin(creds);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(((LoginErrors) response.getBody() ).getPassword().size(), is(1) );
        assertThat(
                ((LoginErrors) response.getBody() ).getPassword().get(0),
                is("Неправильно указан пароль")
        );
    }

    @Test
    public void testPostLoginOk() {
        final String username = "user1";
        final String password = "123pwd";
        UserCreds creds = new UserCreds(username,password);

        final String token = "abcd1212x.zLKL.t789Bgre";

        final User user = new User(1L,username, password);

        jwtHelper = mock(JwtHelper.class);
        when(jwtHelper.createToken(Map.ofEntries(),username)).thenReturn(token);

        passwordEncoder = mock(BCryptPasswordEncoder.class);
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);

        userRepository = mock(UserRepository.class);
        when(userRepository.findOne(any(Example.class))).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(new User(username,password,token));

        authController = new AuthController(jwtHelper,userRepository,passwordEncoder);

        ResponseEntity<?> response = authController.postLogin(creds);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(((Login) response.getBody() ).getToken(), is(token) );
    }
}
