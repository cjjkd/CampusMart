package com.itcjj.campusmart.config;
import com.itcjj.campusmart.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration

public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")                                 // 拦：所有路径
                .excludePathPatterns("/user/login", "/user/register")   // 放：登录和注册
                .excludePathPatterns("/error");                         // 放：Spring 兜底错误页
                // 注意：静态资源（/index.html 等）不在这里列白名单 —— 白名单永远列不全，
                // 已在 LoginInterceptor 里按 handler 类型放行（不是 HandlerMethod 就直接过）
    }
    @Value("${campusmart.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 把 /upload/** 的请求，映射到磁盘上的上传目录
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + uploadPath);
    }

}
