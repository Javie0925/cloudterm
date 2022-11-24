package com.kodedu.cloudterm.handler;

import com.kodedu.cloudterm.dao.entity.SessionEntity;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.LinkedBlockingQueue;

public interface TerminalHandler {

    void onTerminalInit();

    void onTerminalReady();

    void onCommand(String command);

    void onTerminalResize(String columns, String rows);

    void onTerminalClose();

    void setWebSocketSession(WebSocketSession webSocketSession);

    WebSocketSession getWebSocketSession();

    void setSessionEntity(SessionEntity sessionEntity);

    SessionEntity getSessionEntity();

    void print(String text);

    LinkedBlockingQueue<String> getCommandQueue();

    boolean isReady();

    void setReady(boolean ready);

    void init(WebSocketSession session, SessionEntity sessionEntity);
}
