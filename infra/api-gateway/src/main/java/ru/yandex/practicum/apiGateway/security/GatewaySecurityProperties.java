package ru.yandex.practicum.apiGateway.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "gateway.security")
public record GatewaySecurityProperties(
        List<User> users
) {

    public record User(
            String username,
            String password,
            List<String> roles
    ) {
    }
}
