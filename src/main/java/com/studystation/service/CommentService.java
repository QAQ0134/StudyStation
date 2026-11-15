package com.studystation.service;

import com.studystation.pojo.Comment;
import java.util.List;

public interface CommentService {
    void addComment(Comment comment);
    List<Comment> getCommentsByWorkId(Integer workId);
    String deleteComment(Integer commentId, Integer currentUserId);
    String deleteComment(Integer commentId, Integer currentUserId, Integer userRole);
}