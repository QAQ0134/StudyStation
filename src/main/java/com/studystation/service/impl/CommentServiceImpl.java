package com.studystation.service.impl;

import com.studystation.mapper.CommentMapper;
import com.studystation.mapper.WorkMapper;
import com.studystation.pojo.Comment;
import com.studystation.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private WorkMapper workMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String RECOMMEND_KEY = "work:recommend";

    @Override
    public void addComment(Comment comment) {
        // 1. 存数据库
        commentMapper.insert(comment);

        // 2. 评论影响推荐：Redis 推荐分数 +2
        redisTemplate.opsForZSet().incrementScore(RECOMMEND_KEY, comment.getWorkId().toString(), 2);
    }

    @Override
    public List<Comment> getCommentsByWorkId(Integer workId) {
        return commentMapper.findByWorkId(workId);
    }

    @Override
    public String deleteComment(Integer commentId, Integer currentUserId) {
        // 1. 查这条评论是否存在
        Comment comment = commentMapper.getById(commentId);
        if (comment == null) {
            return "评论不存在或已被删除";
        }

        // 2. 查这篇文章的作者 ID
        Integer authorId = workMapper.getAuthorIdByWorkId(comment.getWorkId());

        // 3. 权限判定：如果是文章作者，或者是评论者本人，才可以删除
        if (!currentUserId.equals(authorId) && !currentUserId.equals(comment.getUserId())) {
            return "您没有权限删除这条评论";
        }

        // 4. 执行删除
        commentMapper.deleteById(commentId);
        return null; // 成功返回 null
    }

    @Override
    public String deleteComment(Integer commentId, Integer currentUserId, Integer userRole) {
        Comment comment = commentMapper.getById(commentId);
        if (comment == null) {
            return "评论不存在或已被删除";
        }

        // 1. 如果是管理员，直接放行
        if (userRole != null && userRole == 1) {
            commentMapper.deleteById(commentId);
            return null;
        }

        // 2. 查这篇文章的作者 ID
        Integer authorId = workMapper.getAuthorIdByWorkId(comment.getWorkId());

        // 3. 权限判定：文章作者，或评论者本人
        if (!currentUserId.equals(authorId) && !currentUserId.equals(comment.getUserId())) {
            return "您没有权限删除这条评论";
        }

        commentMapper.deleteById(commentId);
        return null;
    }
}