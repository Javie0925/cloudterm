package com.kodedu.cloudterm.mail;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.Email;

import javax.mail.Folder;
import javax.mail.Session;

@Slf4j
@Data
public class MailConnection  {

    private String connectionId;
    private boolean connected;
    private Email email;
    private Session session;
    private Folder folder;

    public void init() {

    }

    public void sendCmd(String cmd) {

    }

    private String buildMailMessage(String cmd) {
        return null;
    }

    public void connect() {

    }

    public boolean isConnected(){
        return connected;
    }

    public void disconnect() {

    }
}
