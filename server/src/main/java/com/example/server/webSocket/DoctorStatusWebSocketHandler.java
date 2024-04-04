package com.example.server.webSocket;

import com.example.server.doctor.DoctorService;
import com.example.server.dto.response.DoctorStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class DoctorStatusWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private DoctorService doctorService;

    // Return the list of active sessions
    @Getter
    private List<WebSocketSession> sessions = new ArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sendUpdates(session);
    }

    public void sendUpdates(WebSocketSession session) throws IOException {
        List<DoctorStatus> doctorStatusList = doctorService.getDoctorStatus();
        ObjectMapper objectMapper = new ObjectMapper();
        for (DoctorStatus doctorStatus : doctorStatusList) {
            String jsonString = objectMapper.writeValueAsString(doctorStatus);
            session.sendMessage(new TextMessage(jsonString)); // Send each DoctorStatus as a JSON string
        }
    }

}