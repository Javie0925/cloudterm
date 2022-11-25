package com.kodedu.cloudterm.mail;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CmdMailMessage implements MailMessage<String> {

    private String from;

    private String connectionId;

    private String command;

    private TargetServer targetServer;

    private String msgType;

    private Map<String, String> headers = new HashMap<>();

    @Override
    public String getSubject() {
        return null;
    }

    @Override
    public String getContent() {
        return null;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void setHead(String name, String value) {
        this.headers.put(name, value);
    }

    @Data
    class TargetServer {
        private String host;
        private String port;
        private String user;
        private String passwd;
    }
}
