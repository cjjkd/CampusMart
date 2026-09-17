package com.itcjj.campusmart.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CodeEnum {
    SUCCESS(0,"ok"),
    USERNAME_EXIST(1001,"用户名已存在"),
    USER_NOT_FOUND(1002,"用户不存在"),
    LOGIN_FAILED(1003, "用户名或密码错误"),
    NO_PERMISSION(403, "无权限访问"),
    NOT_LOGIN(401, "请先登录");


    private final int code;
    private final String msg;
}
