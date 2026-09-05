package com.pratk.movie_RS.movie_RS.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http.
                authorizeHttpRequests(auth -> auth.requestMatchers("/api/v1/public/**").
                        permitAll().anyRequest().authenticated()).
                oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();

        scopesConverter.setAuthorityPrefix("SCOPE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            Collection<GrantedAuthority> authorities = new HashSet<>(scopesConverter.convert(jwt));

            List<String> rolesAndPermissions = jwt.getClaimAsStringList("authorities");

            System.out.println("JWT AUTHORITIES CLAIM = " + rolesAndPermissions);

            if (rolesAndPermissions != null) {
                rolesAndPermissions.forEach(a -> {
                    System.out.println("ADDING = [" + a + "]");

                    authorities.add(new SimpleGrantedAuthority(a));
                });
            }

            System.out.println("FINAL SPRING AUTHORITIES = " + authorities);

            return authorities;
        });

        return converter;
    }
}