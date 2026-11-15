package com.studystation.aspect;

import com.studystation.annotation.Log;
import com.studystation.mapper.OperationLogMapper;
import com.studystation.pojo.LoginForm;
import com.studystation.pojo.OperationLog;
import com.studystation.pojo.Result;
import com.studystation.pojo.User;
import com.studystation.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
public class LogAspect {

    @Autowired
    private OperationLogMapper logMapper;

    // 切入点：拦截所有带有 @Log 注解的方法
    @Pointcut("@annotation(com.studystation.annotation.Log)")
    public void logPointCut() {}

    // 在方法成功返回后执行
    @AfterReturning(pointcut = "logPointCut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        // 1. 获取方法上的注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Log logAnnotation = method.getAnnotation(Log.class);

        // 2. 构建日志对象
        OperationLog opLog = new OperationLog();
        opLog.setModule(logAnnotation.module());
        opLog.setType(logAnnotation.type());

        // 3. 获取 IP 地址和当前请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
            opLog.setIp(getIpAddress(request));
        }

        // 4. 提取操作人用户名和描述
        Object[] args = joinPoint.getArgs();
        boolean isUsernameFound = false;

        // 优先从参数中获取用户名（针对登录/注册）
        if (args != null && args.length > 0) {
            Object arg = args[0];
            if (arg instanceof LoginForm) {
                opLog.setUsername(((LoginForm) arg).getUsername());
                isUsernameFound = true;
            } else if (arg instanceof User) {
                opLog.setUsername(((User) arg).getUsername());
                isUsernameFound = true;
            }
        }

        // 如果参数里没有用户名（如退出登录），则尝试从 Token 里解析
        if (!isUsernameFound && request != null) {
            String token = request.getHeader("token");
            if (token != null && !token.isEmpty()) {
                try {
                    Claims claims = JwtUtils.parseToken(token);
                    opLog.setUsername(claims.get("username", String.class));
                } catch (Exception e) {
                    opLog.setUsername("未知用户");
                }
            } else {
                opLog.setUsername("游客");
            }
        }

        // 5. 动态判断成功/失败并设置描述
        if (result instanceof Result) {
            Result res = (Result) result;

            if (res.getCode() == 1) {
                // 成功
                if ("登录".equals(opLog.getType())) {
                    opLog.setDescription("登录成功");
                } else if ("注册".equals(opLog.getType())) {
                    opLog.setDescription("注册成功");
                } else if ("退出".equals(opLog.getType())) {
                    opLog.setDescription("退出登录成功");
                } else {
                    opLog.setDescription(logAnnotation.module() + "-操作成功");
                }
            } else {
                // 失败 (记录失败原因)
                String errorMsg = res.getMsg() != null ? res.getMsg() : "未知错误";
                if ("登录".equals(opLog.getType())) {
                    opLog.setDescription("登录失败：" + errorMsg);
                } else if ("注册".equals(opLog.getType())) {
                    opLog.setDescription("注册失败：" + errorMsg);
                } else {
                    opLog.setDescription(logAnnotation.module() + "-操作失败：" + errorMsg);
                }
            }
        } else {
            // 如果返回的不是 Result 类型，默认记录成功
            opLog.setDescription(logAnnotation.module() + "-操作执行完成");
        }

        // 6. 保存到数据库
        try {
            logMapper.insertLog(opLog);
        } catch (Exception e) {
            // 防止日志保存失败影响主业务
            System.err.println("保存操作日志失败: " + e.getMessage());
        }
    }

    // 获取真实 IP 的工具方法
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 本机访问时，IPv6 会显示为 0:0:0:0:0:0:0:1，转为 127.0.0.1
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}