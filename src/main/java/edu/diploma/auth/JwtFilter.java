package edu.diploma.auth;

import edu.diploma.model.User;
import edu.diploma.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Example;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Component
@PropertySource("classpath:/application.properties")
public class JwtFilter extends OncePerRequestFilter {

    @Value("${edu.diploma.security.jwt.authorization-header:auth-token}")
    private String AUTHORIZATION_HEADER;

    private final UserDetailsService userDetailsService;

    private final UserRepository userRepository;

    private final JwtHelper jwtHelper;

    public JwtFilter(
            UserDetailsService userDetailsService,
            UserRepository userRepository,
            JwtHelper jwtHelper
    ) {
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.jwtHelper = jwtHelper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Optional.ofNullable(request.getHeader(AUTHORIZATION_HEADER)).map(
                header -> header.split("\s")
        ).map(
                pieces -> pieces[pieces.length - 1]
        ).filter(
                jwt -> !Objects.isNull(jwt)
        ).map(
                jwt -> Pair.with(jwt,jwtHelper.extractUsername(jwt))
        ).filter(
                jwt_username -> userRepository.findOne(
                        Example.of(new User(jwt_username.getValue1()))
                ).map(
                        x -> jwt_username.getValue0().equals(x.getToken())
                ).orElseGet(() -> false)
        ).map(
                jwt_username -> Pair.with(
                        jwt_username,
                        userDetailsService.loadUserByUsername(jwtHelper.extractUsername(jwt_username.getValue0()))
                )
        ).filter(
                jwt_username_details -> jwtHelper.validateToken(
                        jwt_username_details.getValue0().getValue0(),
                        jwt_username_details.getValue1()
                )
        ).ifPresentOrElse(
                jwt_username_details -> {
                    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                            jwt_username_details.getValue1(),
                            jwt_username_details.getValue0().getValue0(),
                            jwt_username_details.getValue1().getAuthorities()
                    );
                    token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(token);

                    try {
                        filterChain.doFilter(request, response);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                },
                () -> {
                    try {
                        filterChain.doFilter(request, response);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
