package com.studystation.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.studystation.mapper.UserHobbyMapper;
import com.studystation.mapper.UserMapper;
import com.studystation.pojo.LoginForm;
import com.studystation.pojo.PageResult;
import com.studystation.pojo.User;
import com.studystation.pojo.UserQueryParam;
import com.studystation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserHobbyMapper userHobbyMapper;

    @Override
    public List<User> findAll() {
        return userMapper.findAll();
    }

    @Override
    public PageResult<User> pageQuery(Integer pageNum, Integer pageSize) {
        // 1. 设置分页参数
        PageHelper.startPage(pageNum, pageSize);

        // 2. 查询数据（PageHelper 会自动拦截 SQL 加上 LIMIT）
        List<User> userList = userMapper.findAll();

        // 3. 强转为 Page 对象，获取总记录数
        Page<User> page = (Page<User>) userList;

        // 4. 把密码置空，保护隐私
        page.forEach(user -> user.setPassword(null));

        // 5. 封装返回结果
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    @Override
    public PageResult<User> query(UserQueryParam param) {
        // 开启分页
        PageHelper.startPage(param.getPageNum(), param.getPageSize());

        // 执行动态查询
        List<User> userList = userMapper.query(param);

        // 强转 Page 对象
        Page<User> page = (Page<User>) userList;

        // 密码置空
        page.forEach(user -> user.setPassword(null));

        return new PageResult<>(page.getTotal(), page.getResult());
    }

    @Override
    public String updateUserStatus(Integer id, Integer status) {
        // 校验状态值，只允许 0(封禁) 或 1(正常)
        if (status == null || (status != 0 && status != 1)) {
            return "状态值不合法，只能是正常(1)或封禁(0)";
        }
        userMapper.updateStatus(id, status);
        return null;
    }

    @Override
    public String register(User user) {
        // 1. 基础校验
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return "用户名不能为空";
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return "密码不能为空";
        }

        // 2. 查重
        User exist = userMapper.getByUsername(user.getUsername());
        if (exist != null) {
            return "该用户名已被注册，请换一个";
        }

        // 3. 设置默认值（昵称默认与用户名一致，状态正常）
        if (user.getNickname() == null || user.getNickname().trim().isEmpty()) {
            user.setNickname(user.getUsername());
        }
        user.setStatus(1);
        user.setIsDeleted(0);

        // 4. 执行插入
        userMapper.insert(user);
        return null; // 返回 null 代表成功
    }

    @Override
    public String login(LoginForm loginForm) {
        if (loginForm.getUsername() == null || loginForm.getPassword() == null) {
            return "用户名或密码不能为空";
        }

        // 1. 根据用户名查询用户
        User user = userMapper.getByUsername(loginForm.getUsername());

        // 2. 判断用户是否存在
        if (user == null) {
            return "用户名或密码错误";
        }

        // 3. 判断是否已注销
        if (user.getIsDeleted() == 1) {
            return "该账号已注销，无法登录";
        }

        // 4. 判断是否被封禁
        if (user.getStatus() == 0) {
            return "该账号已被封禁，请联系管理员";
        }

        // 5. 校验密码
        if (!user.getPassword().equals(loginForm.getPassword())) {
            return "用户名或密码错误";
        }

        return null; // 返回 null 代表登录成功
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.getByUsername(username);
    }

    @Override
    public User getById(Integer id) {
        User user = userMapper.getById(id);
        if (user != null) {
            user.setPassword(null); // 保护隐私
            // 查出关联的爱好知识点ID
            List<Integer> hobbyIds = userHobbyMapper.getHobbyIdsByUserId(id);
            user.setHobbyIds(hobbyIds);
        }
        return user;
    }

    @Override
    public void updateProfile(User user) {
        // 1. 更新基本信息
        userMapper.updateProfile(user);

        // 2. 更新兴趣爱好（先删后插）
        userHobbyMapper.deleteByUserId(user.getId());
        if (user.getHobbyIds() != null && !user.getHobbyIds().isEmpty()) {
            for (Integer kpId : user.getHobbyIds()) {
                userHobbyMapper.insertHobby(user.getId(), kpId);
            }
        }
    }
}