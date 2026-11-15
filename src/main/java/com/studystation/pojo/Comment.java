package com.studystation.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private Integer id;
    private Integer workId;
    private Integer userId;
    private String content;
    private LocalDateTime createTime;

    // 非数据库字段，用于展示
    private String nickname;
    private String avatar;
    private String username; // 用于判断是否是文章作者
}