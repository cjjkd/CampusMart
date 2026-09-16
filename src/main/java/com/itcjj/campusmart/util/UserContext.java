package com.itcjj.campusmart.util;

public class UserContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    public static void set(Long user) {
        USER_ID.set(user);
    }//存
    public static Long get() {
        return USER_ID.get();
    }//取
    public static void remove() {
        USER_ID.remove();
    }//清
}
