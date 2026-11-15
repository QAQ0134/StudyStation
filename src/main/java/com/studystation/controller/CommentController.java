package com.studystation.controller;

import com.studystation.pojo.Comment;
import com.studystation.pojo.Result;
import com.studystation.service.CommentService;
import com.studystation.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // 发表评论
    @PostMapping
    public Result add(@RequestBody Comment comment, @RequestHeader("token") String token) {
        try {
            Claims claims = JwtUtils.parseToken(token);
            Integer userId = claims.get("id", Integer.class);
            comment.setUserId(userId);
            commentService.addComment(comment);
            return Result.success();
        } catch (Exception e) {
            return Result.error("发表评论失败，请先登录");
        }
    }

    // 获取文章评论列表
    @GetMapping("/work/{workId}")
    public Result list(@PathVariable Integer workId) {
        List<Comment> list = commentService.getCommentsByWorkId(workId);
        return Result.success(list);
    }

    // 删除评论
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id, @RequestHeader("token") String token) {
        try {
            Claims claims = JwtUtils.parseToken(token);
            Integer currentUserId = claims.get("id", Integer.class);
            // 👇 获取当前用户的角色
            Integer userRole = claims.get("role", Integer.class);

            String errorMsg = commentService.deleteComment(id, currentUserId, userRole);
            if (errorMsg != null) {
                return Result.error(errorMsg);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error("操作失败，请先登录");
        }
    }
}