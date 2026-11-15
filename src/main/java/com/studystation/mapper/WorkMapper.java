package com.studystation.mapper;

import com.studystation.pojo.Work;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WorkMapper {

    // 发布作品
    @Insert("INSERT INTO work(user_id, title, content, cover_image, status, create_time, update_time) " +
            "VALUES(#{userId}, #{title}, #{content}, #{coverImage}, 1, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Work work);

    // 更新点击量 (+1)
    @Update("UPDATE work SET view_count = view_count + 1 WHERE id = #{id}")
    void incrViewCount(Integer id);

    // 更新点赞量 (传入 1 表示点赞，-1 表示取消)
    @Update("UPDATE work SET like_count = like_count + #{count} WHERE id = #{id}")
    void updateLikeCount(@Param("id") Integer id, @Param("count") Integer count);

    // 更新收藏量
    @Update("UPDATE work SET collect_count = collect_count + #{count} WHERE id = #{id}")
    void updateCollectCount(@Param("id") Integer id, @Param("count") Integer count);

    // 根据ID查详情（带上作者信息）
    @Select("SELECT w.*, u.nickname as authorName, u.avatar as authorAvatar " +
            "FROM work w LEFT JOIN user u ON w.user_id = u.id WHERE w.id = #{id}")
    Work getById(Integer id);

    // 根据学科ID查询作品（只查已发布的）
    @Select("SELECT w.*, u.nickname as authorName, u.avatar as authorAvatar " +
            "FROM work w " +
            "JOIN work_subject ws ON w.id = ws.work_id " +
            "LEFT JOIN user u ON w.user_id = u.id " +
            "WHERE ws.subject_area_id = #{subjectId} AND w.status = 1 " +
            "ORDER BY w.create_time DESC")
    List<Work> getWorksBySubjectId(Integer subjectId);

    // 检查用户是否点过赞
    @Select("SELECT COUNT(*) FROM user_work_like WHERE user_id = #{userId} AND work_id = #{workId}")
    int checkUserLiked(@Param("userId") Integer userId, @Param("workId") Integer workId);

    // 添加点赞记录
    @Insert("INSERT INTO user_work_like(user_id, work_id) VALUES(#{userId}, #{workId})")
    void insertUserLike(@Param("userId") Integer userId, @Param("workId") Integer workId);

    // 删除点赞记录
    @Delete("DELETE FROM user_work_like WHERE user_id = #{userId} AND work_id = #{workId}")
    void deleteUserLike(@Param("userId") Integer userId, @Param("workId") Integer workId);

    // 检查用户是否收藏过
    @Select("SELECT COUNT(*) FROM user_work_collect WHERE user_id = #{userId} AND work_id = #{workId}")
    int checkUserCollected(@Param("userId") Integer userId, @Param("workId") Integer workId);

    // 添加收藏记录
    @Insert("INSERT INTO user_work_collect(user_id, work_id) VALUES(#{userId}, #{workId})")
    void insertUserCollect(@Param("userId") Integer userId, @Param("workId") Integer workId);

    // 删除收藏记录
    @Delete("DELETE FROM user_work_collect WHERE user_id = #{userId} AND work_id = #{workId}")
    void deleteUserCollect(@Param("userId") Integer userId, @Param("workId") Integer workId);

    // 只查询作品的作者ID
    @Select("SELECT user_id FROM work WHERE id = #{id}")
    Integer getAuthorIdByWorkId(Integer id);

    // 查询当前用户收藏的作品列表
    @Select("SELECT w.*, u.nickname as authorName, u.avatar as authorAvatar " +
            "FROM work w " +
            "JOIN user_work_collect uwc ON w.id = uwc.work_id " +
            "LEFT JOIN user u ON w.user_id = u.id " +
            "WHERE uwc.user_id = #{userId} AND w.status = 1 " +
            "ORDER BY uwc.create_time DESC")
    List<Work> getCollectWorksByUserId(Integer userId);

    // 删除某篇文章的所有点赞记录
    @Delete("DELETE FROM user_work_like WHERE work_id = #{workId}")
    void deleteLikesByWorkId(Integer workId);

    // 删除某篇文章的所有收藏记录
    @Delete("DELETE FROM user_work_collect WHERE work_id = #{workId}")
    void deleteCollectsByWorkId(Integer workId);

    // 删除文章本体
    @Delete("DELETE FROM work WHERE id = #{id}")
    void deleteById(Integer id);

    // 查询指定用户发布的全部作品
    @Select("SELECT w.*, u.nickname as authorName, u.avatar as authorAvatar " +
            "FROM work w " +
            "LEFT JOIN user u ON w.user_id = u.id " +
            "WHERE w.user_id = #{userId} AND w.status = 1 " +
            "ORDER BY w.create_time DESC")
    List<Work> getWorksByUserId(Integer userId);
}