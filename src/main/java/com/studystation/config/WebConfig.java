package com.studystation.config;

import com.studystation.interceptor.AdminInterceptor;
import com.studystation.interceptor.LoginInterceptor;
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

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Value("${studystation.upload.path}")
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 登录拦截器
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                // 放行：登录、注册、上传文件接口、图片资源
                // 放行：获取作品推荐列表和作品详情（游客也能看）
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/upload/**",
                        "/images/**",
                        "/work/recommend",
                        "/work/*", // 这里放行获取作品详情，例如 /work/1
                        "/user/public/**",  // 放行公开的用户信息
                        "/work/userWorks",   // 放行获取作品的接口
                        "/admin/log/**"
                )
                .order(1);

        // 2. 管理员拦截器
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/user", "/user/status")
                .order(2);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 当访问 /images/** 时，去本地 uploadPath 目录下找文件
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath);
    }
}