package com.studystation.pojo;

import lombok.Data;

@Data
public class UserQueryParam {
    private Integer pageNum = 1; // 当前页
    private Integer pageSize = 10; // 每页条数
    private String username; // 用户名（模糊查询）
    private String beginTime; // 注册开始时间
    private String endTime; // 注册结束时间
    private Integer status; // 账户状态：0-封禁，1-正常，2-注销
}