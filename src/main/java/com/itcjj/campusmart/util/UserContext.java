package com.itcjj.campusmart.util;

import com.itcjj.campusmart.common.CurrentUser;

public class UserContext {
    private static final ThreadLocal<CurrentUser> CURRENT = new ThreadLocal<>();
    public static void set(CurrentUser user) {
        CURRENT.set(user);
    }//存
    public static CurrentUser get() {
        return CURRENT.get();
    }//取
    public static void remove() {
        CURRENT.remove();
    }//清
}
