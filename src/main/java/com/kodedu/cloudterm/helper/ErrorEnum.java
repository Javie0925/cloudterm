package com.kodedu.cloudterm.helper;

public enum ErrorEnum {




    ;


    private int code;
    private String msg;

    ErrorEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
