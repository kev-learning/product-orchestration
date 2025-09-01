package com.microservices.core.product.orchestration.service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity security) {
        security.csrf(ServerHttpSecurity.CsrfSpec::disable).authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec.anyExchange().permitAll());

        return security.build();
    }
}
