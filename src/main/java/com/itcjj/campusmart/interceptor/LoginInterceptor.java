package com.itcjj.campusmart.interceptor;

import com.itcjj.campusmart.common.CodeEnum;
import com.itcjj.campusmart.exception.BizException;
import com.itcjj.campusmart.util.JwtUtil;
import com.itcjj.campusmart.util.UserContext;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;
    @Override

    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        // 这里写校验逻辑
        String header = request.getHeader("Authorization");   // 取请求头
        // 判空 → 抛 NOT_LOGIN
        // 截掉 "Bearer " 前缀（7 个字符）
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BizException(CodeEnum.NOT_LOGIN);
        }

        String token = header.substring(7);
        // 解析 token
        try{
        Long userId = jwtUtil.getUserId(token);
        // UserContext.set(userId)
        UserContext.set(userId);
        } catch (JwtException e) {
            throw new BizException(CodeEnum.NOT_LOGIN);
        }
        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        UserContext.remove();
    }

}
