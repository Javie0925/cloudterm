package com.kodedu.cloudterm.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodedu.cloudterm.dao.entity.SessionEntity;
import lombok.SneakyThrows;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractTerminalHandler implements TerminalHandler {

    private LinkedBlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();
    private WebSocketSession webSocketSession;
    private boolean ready;
    private SessionEntity sessionEntity;

    @Override
    public void onTerminalInit() {
    }

    @SneakyThrows
    @Override
    public void print(String text) {

        Map<String, String> map = new HashMap<>();
        map.put("type", "TERMINAL_PRINT");
        map.put("text", text);

        String message = new ObjectMapper().writeValueAsString(map);

        getWebSocketSession().sendMessage(new TextMessage(message));
    }

    @Override
    public void setSessionEntity(SessionEntity sessionEntity) {
        this.sessionEntity = sessionEntity;
    }

    @Override
    public SessionEntity getSessionEntity() {
        return this.sessionEntity;
    }

    @Override
    public LinkedBlockingQueue<String> getCommandQueue() {
        return this.commandQueue;
    }

    @Override
    public void onTerminalResize(String columns, String rows) {

    }

    @Override
    public void setWebSocketSession(WebSocketSession webSocketSession) {
        this.webSocketSession = webSocketSession;
    }

    @Override
    public WebSocketSession getWebSocketSession() {
        return this.webSocketSession;
    }

    @Override
    public boolean isReady() {
        return this.ready;
    }

    @Override
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    @Override
    public void init(WebSocketSession session, SessionEntity sessionEntity) {
        setWebSocketSession(session);
        setSessionEntity(sessionEntity);
    }
}
