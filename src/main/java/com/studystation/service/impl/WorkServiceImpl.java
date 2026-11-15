package com.studystation.service.impl;

import com.studystation.mapper.CommentMapper;
import com.studystation.mapper.WorkMapper;
import com.studystation.mapper.WorkRelationMapper;
import com.studystation.pojo.Work;
import com.studystation.service.WorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class WorkServiceImpl implements WorkService {

    @Autowired
    private WorkMapper workMapper;

    @Autowired
    private WorkRelationMapper workRelationMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CommentMapper commentMapper;


    private static final String RECOMMEND_KEY = "work:recommend";

    @Override
    @Transactional
    public void publishWork(Work work, Integer userId) {
        work.setUserId(userId);
        // 1. 存数据库
        workMapper.insert(work);

        // 2. 存关联表
        if (work.getSubjectIds() != null) {
            for (Integer subId : work.getSubjectIds()) {
                workRelationMapper.insertWorkSubject(work.getId(), subId);
            }
        }
        if (work.getKnowledgePointIds() != null) {
            for (Integer kpId : work.getKnowledgePointIds()) {
                workRelationMapper.insertWorkKp(work.getId(), kpId);
            }
        }

        // 3. 写入 Redis 推荐池 (初始分数 = 当前时间戳)
        double score = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(RECOMMEND_KEY, work.getId().toString(), score);
    }

    @Override
    public Work getWorkDetail(Integer id) {
        // 1. 查详情
        Work work = workMapper.getById(id);
        if (work != null) {
            work.setSubjectIds(workRelationMapper.getSubjectIdsByWorkId(id));
            work.setKnowledgePointIds(workRelationMapper.getKpIdsByWorkId(id));

            // 把知识点名称查出来放进去
            work.setKnowledgeTags(workRelationMapper.getKpTagsByWorkId(id));

            // 2. 增加点击量
            addViewCount(id);
        }
        return work;
    }

    @Override
    public void addViewCount(Integer id) {
        // 数据库 +1
        workMapper.incrViewCount(id);
        // Redis 分数 +1 (点击权重为 1)
        redisTemplate.opsForZSet().incrementScore(RECOMMEND_KEY, id.toString(), 1);
    }

    @Override
    @Transactional // 保证数据库操作一致性
    public void likeWork(Integer id, Integer userId, boolean isLike) {
        if (isLike) {
            // 如果是要点赞，先查是否已经点过
            if (workMapper.checkUserLiked(userId, id) == 0) {
                workMapper.insertUserLike(userId, id); // 记录到关联表
                workMapper.updateLikeCount(id, 1); // 总赞数 +1
                redisTemplate.opsForZSet().incrementScore(RECOMMEND_KEY, id.toString(), 5); // Redis +5分
            }
        } else {
            // 取消点赞
            if (workMapper.checkUserLiked(userId, id) > 0) {
                workMapper.deleteUserLike(userId, id); // 删除关联记录
                workMapper.updateLikeCount(id, -1); // 总赞数 -1
                redisTemplate.opsForZSet().incrementScore(RECOMMEND_KEY, id.toString(), -5); // Redis -5分
            }
        }
    }

    @Override
    @Transactional
    public void collectWork(Integer id, Integer userId, boolean isCollect) {
        if (isCollect) {
            if (workMapper.checkUserCollected(userId, id) == 0) {
                workMapper.insertUserCollect(userId, id);
                workMapper.updateCollectCount(id, 1);
                redisTemplate.opsForZSet().incrementScore(RECOMMEND_KEY, id.toString(), 10); // 收藏 +10分
            }
        } else {
            if (workMapper.checkUserCollected(userId, id) > 0) {
                workMapper.deleteUserCollect(userId, id);
                workMapper.updateCollectCount(id, -1);
                redisTemplate.opsForZSet().incrementScore(RECOMMEND_KEY, id.toString(), -10);
            }
        }
    }

    @Override
    public Map<String, Boolean> getUserInteractionStatus(Integer workId, Integer userId) {
        Map<String, Boolean> map = new HashMap<>();
        map.put("isLiked", workMapper.checkUserLiked(userId, workId) > 0);
        map.put("isCollected", workMapper.checkUserCollected(userId, workId) > 0);
        return map;
    }

    @Override
    public List<Work> getRecommendWorks() {
        // 从 Redis 获取分数最高的前 10 个
        Set<String> workIds = redisTemplate.opsForZSet().reverseRange(RECOMMEND_KEY, 0, 9);
        List<Work> works = new ArrayList<>();
        if (workIds != null) {
            for (String idStr : workIds) {
                Work work = workMapper.getById(Integer.parseInt(idStr));
                if (work != null) works.add(work);
            }
        }
        return works;
    }

    @Override
    public List<Work> getWorksBySubjectId(Integer subjectId) {
        return workMapper.getWorksBySubjectId(subjectId);
    }

    @Override
    public List<Work> getCollectWorksByUserId(Integer userId) {
        return workMapper.getCollectWorksByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务
    public String deleteWork(Integer workId, Integer currentUserId, Integer userRole) {
        Work work = workMapper.getById(workId);
        if (work == null) {
            return "文章不存在或已被删除";
        }

        // 权限校验：只有作者本人或管理员(role=1)可以删除
        if (!currentUserId.equals(work.getUserId()) && userRole != 1) {
            return "您没有权限删除这篇文章";
        }

        // 1. 删除所有评论
        commentMapper.deleteByWorkId(workId);

        // 2. 删除用户的点赞和收藏记录
        workMapper.deleteLikesByWorkId(workId);
        workMapper.deleteCollectsByWorkId(workId);

        // 3. 删除关联表（学科、知识点）
        workRelationMapper.deleteSubjectsByWorkId(workId);
        workRelationMapper.deleteKpsByWorkId(workId);

        // 4. 删除文章本体
        workMapper.deleteById(workId);

        // 5. 从 Redis 推荐池中移除
        redisTemplate.opsForZSet().remove("work:recommend", workId.toString());

        return null; // 成功
    }

    @Override
    public List<Work> getWorksByUserId(Integer userId) {
        return workMapper.getWorksByUserId(userId);
    }
}