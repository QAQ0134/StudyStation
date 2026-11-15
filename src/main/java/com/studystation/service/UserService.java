package com.studystation.service;

import com.studystation.pojo.LoginForm;
import com.studystation.pojo.PageResult;
import com.studystation.pojo.User;
import com.studystation.pojo.UserQueryParam;

import java.util.List;

public interface UserService {
    List<User> findAll();

    PageResult<User> pageQuery(Integer pageNum, Integer pageSize);

    PageResult<User> query(UserQueryParam param);

    String updateUserStatus(Integer id, Integer status);

    String register(User user);

    String login(LoginForm loginForm);

    User getByUsername(String username);

    User getById(Integer id);

    void updateProfile(User user);
}