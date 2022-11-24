package com.kodedu.cloudterm.dao.entity;

import lombok.Data;

@Data
public class SessionEntity {

    private String id;
    private String name;
    private String host;
    private int port;
    private String passwd;
    private String user;
    private String note;
    private String type = "jsch";
}
