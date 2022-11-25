package com.kodedu.cloudterm.mail;

import java.util.Map;

interface MailMessage<T> {

    String getSubject();

    T getContent();

    Map<String, String> getHeaders();

    void setHead(String name,String value);
}
