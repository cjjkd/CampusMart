package com.itcjj.campusmart.common;

import lombok.Data;
@Data
public class Result<T> {
    private int code;
    private String msg;
    private T data;
    public static <T> Result<T> success(T data){
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMsg("ok");
        result.setData(data);
        return result;
    }
    public static <T> Result<T> error(int code,String msg){
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

}
