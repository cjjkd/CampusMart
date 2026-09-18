package com.itcjj.campusmart.interceptor;

import com.itcjj.campusmart.annotation.RequireAdmin;
import com.itcjj.campusmart.common.CodeEnum;
import com.itcjj.campusmart.common.CurrentUser;
import com.itcjj.campusmart.entity.User;
import com.itcjj.campusmart.exception.BizException;
import com.itcjj.campusmart.mapper.UserMapper;
import com.itcjj.campusmart.util.JwtUtil;
import com.itcjj.campusmart.util.UserContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserMapper userMapper;

    @Override

    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        // 不是 Controller 方法 → 是静态资源（html / css / js / 图片），直接放行，不校验 token
        // 为什么必须放行：index.html 里装着登录页，如果连它都要 token，用户永远进不来（死锁）
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 这里写校验逻辑
        String header = request.getHeader("Authorization");   // 取请求头
        // 判空 → 抛 NOT_LOGIN
        // 截掉 "Bearer " 前缀（7 个字符）
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BizException(CodeEnum.NOT_LOGIN);
        }

        String token = header.substring(7);
        // 解析 token
        Long userId;
        String role;
        Integer ver;
        try{
            Claims claims = jwtUtil.parseToken(token);
            userId = Long.valueOf(claims.getSubject());
            role = claims.get("role", String.class);
            ver=claims.get("ver", Integer.class);
        } catch (JwtException e) {
            throw new BizException(CodeEnum.NOT_LOGIN);
        }
        User user = userMapper.selectById(userId);
        if(user ==null||ver==null||!ver.equals(user.getTokenVersion())){
            throw new BizException(CodeEnum.NOT_LOGIN);
        }
        //读标签校验角色
        if(handler instanceof HandlerMethod handlerMethod) {
            RequireAdmin requireAdmin = handlerMethod.getMethodAnnotation(RequireAdmin.class);
            // 不是admin，拒绝
            if (requireAdmin != null&&!"ADMIN".equals(role)) {
                throw new BizException(CodeEnum.NO_PERMISSION);
            }
        }
        UserContext.set(new CurrentUser(userId, role));
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
