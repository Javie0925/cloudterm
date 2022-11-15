package com.kodedu.cloudterm.dao.entity;

import lombok.Data;

@Data
public class Server {

    private String id;
    private String name;
    private String host;
    private String port;
    private String passwd;
    private String user;
}
