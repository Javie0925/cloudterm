package com.kodedu.cloudterm.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
//@Service
public class MailServer {

    private Session session;
    private Store store;
    @Value("${mail.debug:false}")
    private String debug;

    @Value("${mail.host}")
    private String mailHost;

    // server mail
    private String serverUsername = System.getProperty("server.mail.username");
    private String serverPasswd = System.getProperty("server.mail.passwd");
    // client mail
    private String clientUsername = System.getProperty("client.mail.username");
    private String clientPasswd = System.getProperty("client.mail.passwd");

    public final static String INBOX = "inbox";

    private Map<String, Folder> openedFolderMap = new ConcurrentHashMap<>();

    @SneakyThrows
    public synchronized Folder openFolder(String folderName) {
        checkConnect();
        Folder result = openedFolderMap.computeIfAbsent(folderName, name -> {
            try {
                return store.getFolder(name);
            } catch (MessagingException e) {
                log.error("get folder [{}] fail!", name, e);
                return null;
            }
        });
        Assert.notNull(result, String.format("open folder [%s] fail!", folderName));
        if (!result.isOpen()) {
            openedFolderMap.remove(folderName);
            return openFolder(folderName);
        }
        return result;
    }

    private synchronized void checkConnect() {
        if (Objects.isNull(session) || Objects.isNull(store) || !store.isConnected()) {
            init();
        }
    }

    /**
     * send text mail
     */
    @SneakyThrows
    public void sendTextMail(String text) {
        HtmlEmail email = new HtmlEmail();
        email.setHostName("imap.qq.com");
        email.setSSLOnConnect(true);
        email.setSmtpPort(465);
        email.setCharset(Charset.defaultCharset().name());
        // server mail
        email.addTo(serverUsername);
        // client mail
        email.setFrom(clientUsername);
        // setAuthentication
        email.setAuthentication(clientUsername, clientPasswd);
        email.setSubject("subject");
        email.setMsg(text);
        // send
        email.send();
    }

    /**
     * init session and store
     */
    @SneakyThrows
    @PostConstruct
    public void init() {
        Assert.hasText(serverUsername, "mail username can not be empty!");
        Assert.hasText(serverPasswd, "mail password can not be empty!");
        log.info("initializing session and store...");
        Properties prop = new Properties();
        prop.setProperty("mail.debug", debug);
        prop.setProperty("mail.store.protocol", "imap");
        prop.setProperty("mail.host", mailHost);
        session = Session.getInstance(prop);
        store = session.getStore("imap");
        store.connect("imap.qq.com", serverUsername, serverPasswd);
        log.info("initialize session and store succeeded.");
    }

    @PreDestroy
    public void clear() {
        if (!openedFolderMap.isEmpty()) {
            openedFolderMap.entrySet().stream().map(en -> en.getValue()).forEach(f -> {
                if (f.isOpen()) {
                    try {
                        f.close();
                    } catch (MessagingException e) {
                        log.error("close folder [%s] fail!", f.getName(), e);
                    }
                }
            });
        }
        if (Objects.nonNull(store) && store.isConnected()) {
            try {
                store.close();
            } catch (MessagingException e) {
                log.error("close store fail!", e);
            }
        }
    }
}
