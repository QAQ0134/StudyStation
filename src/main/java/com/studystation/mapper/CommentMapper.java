package com.studystation.mapper;

import com.studystation.pojo.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

    // 插入评论
    @Insert("INSERT INTO comment(work_id, user_id, content, create_time) VALUES(#{workId}, #{userId}, #{content}, NOW())")
    void insert(Comment comment);

    // 查询某篇文章的所有评论（带上评论人信息）
    @Select("SELECT c.*, u.username, u.nickname, u.avatar " +
            "FROM comment c LEFT JOIN user u ON c.user_id = u.id " +
            "WHERE c.work_id = #{workId} " +
            "ORDER BY c.create_time DESC")
    List<Comment> findByWorkId(Integer workId);

    // 删除评论
    @Delete("DELETE FROM comment WHERE id = #{id}")
    void deleteById(Integer id);

    // 根据ID查询单条评论（用于鉴权校验）
    @Select("SELECT * FROM comment WHERE id = #{id}")
    Comment getById(Integer id);

    // 根据文章ID删除所有评论
    @Delete("DELETE FROM comment WHERE work_id = #{workId}")
    void deleteByWorkId(Integer workId);
}