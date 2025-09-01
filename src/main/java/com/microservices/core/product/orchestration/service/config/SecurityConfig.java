package com.microservices.core.product.orchestration.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity security) {

        security
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec.pathMatchers("/openapi/**", "/webjars/**", "/actuator/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/product-orchestration-service/**").hasAnyAuthority("SCOPE_product:write")
                        .pathMatchers(HttpMethod.GET, "/product-orchestration-service/**").hasAnyAuthority("SCOPE_product:read")
                        .pathMatchers(HttpMethod.DELETE, "/product-orchestration-service/**").hasAnyAuthority("SCOPE_product:write")
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oAuth2ResourceServerSpec -> oAuth2ResourceServerSpec.jwt(Customizer.withDefaults()));

        return security.build();
    }
}
