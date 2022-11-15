package com.kodedu.cloudterm.helper;

public class BizException extends RuntimeException {

    private ErrorEnum errorEnum;

    public BizException() {
    }

    public BizException(ErrorEnum errorEnum) {
        super();
        this.errorEnum = errorEnum;
    }

    public ErrorEnum getErrorEnum() {
        return errorEnum;
    }

    public void setErrorEnum(ErrorEnum errorEnum) {
        this.errorEnum = errorEnum;
    }

}
