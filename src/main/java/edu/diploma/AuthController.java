package edu.diploma;

import edu.diploma.auth.JwtHelper;
import edu.diploma.model.LoginErrors;
import edu.diploma.model.Login;
import edu.diploma.auth.UserCreds;
import edu.diploma.model.User;
import edu.diploma.repository.UserRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class AuthController {

    private final JwtHelper jwtHelper;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(
            JwtHelper jwtHelper,
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.jwtHelper = jwtHelper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @PostMapping("/login")
    ResponseEntity<?> postLogin(@RequestBody UserCreds creds) {

        LoginErrors errors = new LoginErrors();

        if (Objects.isNull(creds)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    errors.addEmailMsg("Неправильные учетные данные")
            );
        }

        if (Objects.isNull(creds.getLogin()) || creds.getLogin().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    errors.addEmailMsg("Необходимо ввести почту")
            );
        }

        if (Objects.isNull(creds.getPassword()) || creds.getPassword().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    errors.addPasswordMsg("Необходимо ввести пароль")
            );
        }

        Optional<User> user = userRepository.findOne(Example.of(
                new User(creds.getLogin()),
                ExampleMatcher.matching()
                        .withMatcher("username", m -> m.exact())
                        .withIgnorePaths("password")
        ));

        if (user.isPresent()) {
            if (passwordEncoder.matches(creds.getPassword(), user.get().getPassword())) {
                user.get().setToken(jwtHelper.createToken(Map.ofEntries(), user.get().getUsername()));
                userRepository.save(user.get());
                return ResponseEntity.ok().body(new Login(user.get().getToken()));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        errors.addPasswordMsg("Неправильно указан пароль")
                );
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    errors.addEmailMsg("Неправильно указана почта")
            );
        }
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<LoginErrors> handleException(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new LoginErrors().addEmailMsg("Неправильные учетные данные")
        );
    }
}