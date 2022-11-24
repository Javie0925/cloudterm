package com.kodedu.cloudterm.service;

import com.kodedu.cloudterm.dao.SessionListDao;
import com.kodedu.cloudterm.dao.entity.SessionEntity;
import com.kodedu.cloudterm.handler.TerminalHandler;
import com.kodedu.cloudterm.handler.TerminalHandlerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.util.Objects;

@Slf4j
@Component
@Scope("prototype")
public class TerminalService {

    private TerminalHandler terminalHandler;

    @Resource
    private SessionListDao sessionListDao;

    public void init(WebSocketSession session, String type, String sessionId) {
        Assert.notNull(session, "session can not be null!");
        SessionEntity sessionEntity = null;
        if (StringUtils.hasText(sessionId)) {
            sessionEntity = sessionListDao.findById(sessionId);
            terminalHandler = TerminalHandlerFactory.getHandler(sessionEntity.getType());
        } else {
            this.terminalHandler = TerminalHandlerFactory.DEFAULT.getHandler();
        }
        terminalHandler.init(session, sessionEntity);
    }

    public void onTerminalInit() {
        terminalHandler.onTerminalInit();
    }

    public void onTerminalReady() {
        try {
            terminalHandler.onTerminalReady();
        } catch (Exception e) {
            terminalHandler.print(e.getMessage());
        }
    }

    public void onCommand(String command) {
        if (Objects.nonNull(command))
            terminalHandler.onCommand(command);

    }

    public void onTerminalResize(String columns, String rows) {
        terminalHandler.onTerminalResize(columns, rows);
    }

    public void onTerminalClose() {
        terminalHandler.onTerminalClose();
    }
}
