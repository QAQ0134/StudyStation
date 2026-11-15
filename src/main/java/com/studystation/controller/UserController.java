package com.studystation.controller;

import com.studystation.annotation.Log;
import com.studystation.pojo.*;
import com.studystation.service.UserService;
import com.studystation.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 分页查询用户 (默认第一页，每页 10 条)
    @GetMapping
    public Result page(UserQueryParam param) {
        PageResult<User> pageResult = userService.query(param);
        return Result.success(pageResult);
    }

    // 修改用户状态 (只允许 0-封禁, 1-正常)
    @PutMapping("/status")
    public Result updateStatus(@RequestParam Integer id, @RequestParam Integer status) {
        String errorMsg = userService.updateUserStatus(id, status);
        if (errorMsg != null) {
            return Result.error(errorMsg);
        }
        return Result.success();
    }

    // 注册接口
    @Log(module = "用户模块", type = "注册")
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        String errorMsg = userService.register(user);
        if (errorMsg != null) {
            return Result.error(errorMsg);
        }
        return Result.success();
    }

    @PostMapping("/login")
    @Log(module = "用户模块", type = "登录")
    public Result login(@RequestBody LoginForm loginForm) {
        String errorMsg = userService.login(loginForm);
        if (errorMsg != null) {
            return Result.error(errorMsg);
        }

        // 登录成功，获取用户完整信息（为了拿 role 和 id）
        User user = userService.getByUsername(loginForm.getUsername());

        // 生成 JWT 令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole()); // 关键：把角色放进去
        String token = JwtUtils.generateToken(claims);

        // 返回 Token 和 角色给前端
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("role", user.getRole());

        return Result.success(data);
    }

    // 退出登录接口
    @Log(module = "用户模块", type = "退出")
    @PostMapping("/logout")
    public Result logout() {
        // JWT 是无状态的，后端其实不需要做什么清理操作
        // 只是为了让 AOP 切面能拦截到这个请求并记录日志
        return Result.success("已退出登录");
    }

    // 获取当前登录用户信息
    @GetMapping("/info")
    public Result getUserInfo(@RequestHeader("token") String token) {
        try {
            Claims claims = JwtUtils.parseToken(token);
            Integer id = claims.get("id", Integer.class);
            User user = userService.getById(id);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("获取用户信息失败");
        }
    }

    // 更新个人信息
    @PutMapping("/info")
    public Result updateUserInfo(@RequestHeader("token") String token, @RequestBody User user) {
        try {
            Claims claims = JwtUtils.parseToken(token);
            Integer id = claims.get("id", Integer.class);
            user.setId(id); // 强制使用 Token 里的 ID，防止前端篡改
            userService.updateProfile(user);
            return Result.success();
        } catch (Exception e) {
            return Result.error("更新失败");
        }
    }

    // 获取指定用户的信息（公开资料，不包含密码等隐私）
    @GetMapping("/public/{id}")
    public Result getPublicUserInfo(@PathVariable Integer id) {
        User user = userService.getById(id);
        if (user == null) return Result.error("用户不存在");

        // 构建只包含公开信息的对象返回（你直接把 User 返回也可以，反正之前 getById 里密码已经置空了）
        return Result.success(user);
    }
}