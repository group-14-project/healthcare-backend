package com.example.server.config;

import com.example.server.jwtToken.JWTService;
import com.example.server.jwtToken.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.CrossOrigin;

@EnableWebSecurity
@Configuration
public class SecurityConfig{

    @Autowired
    private JWTService jwtService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception
    {
        //TODO: we have to include cors and csrf in the production env
        http.csrf(AbstractHttpConfigurer::disable);

        http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests((authorize)->
                authorize.requestMatchers(HttpMethod.GET, "*/landingPage", "*/opdtimings", "*/profile", "/hospitals").permitAll()
                        .requestMatchers(HttpMethod.POST, "*/login", "*/signup", "*/loginotp", "*/signupotp", "/forgotPassword", "/changePassword").permitAll()
                        .requestMatchers("/patient/**").hasRole("patient")
                        .requestMatchers("/doctor/**").hasAnyRole("doctor", "seniorDoctor")
                        .requestMatchers("/doctor/**", "/senior_doctor/**", "/getCallDetails").hasRole("seniorDoctor")
                        .requestMatchers("/hospital/**").hasRole("hospital")
                        .requestMatchers("/doctor-status").permitAll()
                        .requestMatchers("/socket/**").permitAll()
                        .requestMatchers("/app/**").permitAll()
                        .requestMatchers("/user/**").permitAll()


                        .anyRequest().authenticated())
                        .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean for static password
    @Bean
    public String staticPassword() {
        return passwordEncoder().encode("password");
    }
}
