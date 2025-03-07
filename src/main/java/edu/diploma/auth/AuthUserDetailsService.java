package edu.diploma.auth;

import edu.diploma.model.User;
import edu.diploma.repository.UserRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AuthUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("username", matcher -> matcher.exact());

        return userRepository.findOne(Example.of(new User(username))).orElseThrow(
                () -> new UsernameNotFoundException("UsernameNotFoundException: " + username)
        );
    }
}
