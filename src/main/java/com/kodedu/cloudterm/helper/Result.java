package com.kodedu.cloudterm.helper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result {

    private static final Result SUCCESS = new Result();
    private static final int SUCCESS_CODE = 0;

    private int code = 0;
    private String error;
    private Object data;

    public static Result success() {
        return SUCCESS;
    }

    public static Result success(Object data) {
        return new Result(SUCCESS_CODE, null, data);
    }

    public static Result fail(String error) {
        return new Result(500, error, null);
    }


}
