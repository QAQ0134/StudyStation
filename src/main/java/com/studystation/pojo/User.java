package com.studystation.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Integer id; // 主键ID

    private String username; // 用户名（唯一）

    private String password; // 密码（加密存储）

    private String nickname; // 昵称

    private String avatar; // 头像URL

    private String signature; // 个性签名

    private String hobby; // 兴趣爱好

    private Integer gender; // 性别：0-女，1-男，2-保密（用数字存比中文更规范）

    private String region; // 地区

    private Integer status; // 账号状态：1-正常，0-禁用

    private Integer isDeleted; // 逻辑删除：0-未删除，1-已删除

    private LocalDateTime createTime; // 注册时间（自动生成）

    private LocalDateTime updateTime; // 最后修改时间

    private Integer role; // 角色：0-普通用户，1-管理员

    private List<Integer> hobbyIds;
}