package com.example.server.webSocket;

import com.example.server.doctor.DoctorService;
import com.example.server.dto.response.DoctorStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DoctorStatusWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private DoctorService doctorService;

    // Use a Set to store active sessions to handle concurrent modifications
    @Getter
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("WebSocket connection established: " + session.getId());
        sendUpdates(session);
    }

    public void sendUpdatesToAll() {
        System.out.println("FirstTime");
        synchronized (sessions) {
            for (WebSocketSession session : sessions) {
                try {
                    System.out.println(
                            "send updates to all man info going"
                    );
                    sendUpdates(session);
                } catch (IOException e) {
                    handleWebSocketError(session, e);
                }
            }
        }
    }

    public void sendUpdates(WebSocketSession session) throws IOException {
        List<DoctorStatus> doctorStatusList = doctorService.getDoctorStatus();
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(
                "per man info going"
        );
        String jsonString = objectMapper.writeValueAsString(doctorStatusList);
        session.sendMessage(new TextMessage(jsonString));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("WebSocket connection closed: " + session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("WebSocket transport error occurred: " + session.getId());
        handleWebSocketError(session, exception);
    }

    private void handleWebSocketError(WebSocketSession session, Throwable exception) {
        if (session.isOpen()) {
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (IOException e) {
                System.out.println("Error while closing WebSocket session: " + session.getId());
            }
        }
        sessions.remove(session);
    }
}
