package com.studystation.interceptor;

import com.studystation.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 放行 OPTIONS 预检请求（跨域用的）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 2. 从请求头中获取 token
        String token = request.getHeader("token");

        // 3. 判断 token 是否存在
        if (token == null || token.isEmpty()) {
            response.setStatus(401); // 401 代表未授权
            return false;
        }

        // 4. 校验 token 是否有效（解析失败会抛出异常）
        try {
            JwtUtils.parseToken(token);
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }

        // 5. 校验通过，放行
        return true;
    }
}