package com.studystation.interceptor;

import com.studystation.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            response.setStatus(401);
            return false;
        }

        try {
            Claims claims = JwtUtils.parseToken(token);
            // 获取 token 中的 role
            Integer role = claims.get("role", Integer.class);

            // 校验是否是管理员
            if (role == null || role != 1) {
                response.setStatus(403); // 403 代表无权限
                return false;
            }
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
        return true;
    }
}