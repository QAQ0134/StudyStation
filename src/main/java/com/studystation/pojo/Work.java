package com.studystation.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Work {
    private Integer id;
    private Integer userId;
    private String title;
    private String content;
    private String coverImage;
    private Integer knowledgePointId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 非数据库字段，联表查询用于前端展示
    private String authorName;
    private String authorAvatar;

    private List<Integer> subjectIds;
    private List<Integer> knowledgePointIds;

    private Integer viewCount;    // 点击量
    private Integer likeCount;    // 点赞量
    private Integer collectCount; // 收藏量

    private List<KnowledgePointTag> knowledgeTags;
}