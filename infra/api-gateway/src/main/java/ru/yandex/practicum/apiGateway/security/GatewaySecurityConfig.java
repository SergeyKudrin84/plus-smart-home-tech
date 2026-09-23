package ru.yandex.practicum.apiGateway.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(GatewaySecurityProperties.class)
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/inventory/**").permitAll()

                        .pathMatchers(HttpMethod.POST, "/api/orders/**").hasRole("USER")
                        .pathMatchers(HttpMethod.GET, "/api/orders/by-email").hasRole("USER")
                        .pathMatchers(HttpMethod.GET, "/api/orders/*").hasRole("USER")
                        .pathMatchers(HttpMethod.GET, "/api/orders").hasRole("ADMIN")

                        .pathMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/products/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                        .pathMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/categories/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")

                        .pathMatchers(HttpMethod.POST, "/api/inventory/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/inventory/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/inventory/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/inventory/**").hasRole("ADMIN")
                        .anyExchange().denyAll()
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService(
            GatewaySecurityProperties properties,
            PasswordEncoder passwordEncoder
    ) {
        List<UserDetails> users = properties.users().stream()
                .map(user -> User.withUsername(user.username())
                        .password(passwordEncoder.encode(user.password()))
                        .roles(user.roles().toArray(String[]::new))
                        .build())
                .toList();

        return new MapReactiveUserDetailsService(users.toArray(UserDetails[]::new));
    }
}
