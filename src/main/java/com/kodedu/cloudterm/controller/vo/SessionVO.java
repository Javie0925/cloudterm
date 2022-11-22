package com.kodedu.cloudterm.controller.vo;

import lombok.Data;

@Data
public class SessionVO {

    private String id;
    private String name;
    private String host;
    private int port;
    private String note;
    private String user;

}
