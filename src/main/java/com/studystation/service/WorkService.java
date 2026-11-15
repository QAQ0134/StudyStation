package com.studystation.service;

import com.studystation.pojo.Work;
import java.util.List;
import java.util.Map;

public interface WorkService {
    void publishWork(Work work, Integer userId);
    List<Work> getRecommendWorks();
    Work getWorkDetail(Integer id);
    List<Work> getWorksBySubjectId(Integer subjectId);
    void addViewCount(Integer id);
    void likeWork(Integer id, Integer userId, boolean isLike);
    void collectWork(Integer id, Integer userId, boolean isCollect);
    // 查询当前用户对该作品的点赞/收藏状态
    Map<String, Boolean> getUserInteractionStatus(Integer workId, Integer userId);
    List<Work> getCollectWorksByUserId(Integer userId);
    String deleteWork(Integer workId, Integer currentUserId, Integer userRole);
    List<Work> getWorksByUserId(Integer userId);
}