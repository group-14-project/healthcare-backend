package com.example.server.config;

import com.example.server.webSocket.DoctorStatusWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@EnableScheduling
public class WebSocketConfig implements WebSocketConfigurer {

    private final DoctorStatusWebSocketHandler doctorStatusWebSocketHandler;

    @Autowired
    public WebSocketConfig(DoctorStatusWebSocketHandler doctorStatusWebSocketHandler) {
        this.doctorStatusWebSocketHandler = doctorStatusWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(doctorStatusWebSocketHandler, "/doctor-status").setAllowedOrigins("*");
    }
}
