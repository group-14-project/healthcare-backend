package com.example.server.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfig{
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception
    {
        //TODO: we have to include cors and csrf in the production env
        http.csrf(AbstractHttpConfigurer::disable);

        http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests((authorize)->
                authorize.requestMatchers(HttpMethod.GET, "/arogyashala", "*/opdtimings", "*/profile", "/hospitals").permitAll()
                        .requestMatchers(HttpMethod.POST, "*/login", "*/signup", "*/email").permitAll()
                        .anyRequest().authenticated());

        return http.build();
    }
}
