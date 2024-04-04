package com.example.server.webSocket;

import com.example.server.doctor.DoctorService;
import com.example.server.dto.response.DoctorStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;

@Component
public class DoctorStatusScheduler {
    @Autowired
    private DoctorStatusWebSocketHandler doctorStatusWebSocketHandler;

    @Autowired
    private DoctorService doctorService; // Inject DoctorService to fetch doctor status data

    @Scheduled(fixedRate = 50000)
    public void sendDoctorStatusUpdate() {
        List<WebSocketSession> sessions = doctorStatusWebSocketHandler.getSessions();
        for (WebSocketSession session : sessions) {
            try {
                doctorStatusWebSocketHandler.sendUpdates(session);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}