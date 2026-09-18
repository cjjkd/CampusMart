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
    OLD_PASSWORD_WRONG(1004, "原密码错误"),
    FILE_EMPTY(1005, "上传文件不能为空"),
    FILE_TYPE_NOT_ALLOWED(1006, "只支持 jpg / jpeg / png / gif / webp 格式的图片"),
    FILE_UPLOAD_FAILED(1007, "文件上传失败"),


    NOT_LOGIN(401, "请先登录");


    private final int code;
    private final String msg;
}
