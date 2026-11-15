package com.studystation.mapper;

import com.studystation.pojo.User;
import com.studystation.pojo.UserQueryParam;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    // 查询所有未删除的用户（删除了 hobby）
    @Select("SELECT id, username, password, nickname, avatar, signature, gender, region, status, is_deleted, create_time, update_time, role FROM user")
    List<User> findAll();

    // 动态条件查询所有用户（删除了 hobby）
    @Select("<script>" +
            "SELECT id, username, password, nickname, avatar, signature, gender, region, status, is_deleted, create_time, update_time, role FROM user " +
            "<where>" +
            "   <if test='username != null and username != \"\"'>" +
            "       AND username LIKE CONCAT('%', #{username}, '%')" +
            "   </if>" +
            "   <if test='beginTime != null and beginTime != \"\"'>" +
            "       AND create_time &gt;= #{beginTime}" +
            "   </if>" +
            "   <if test='endTime != null and endTime != \"\"'>" +
            "       AND create_time &lt;= #{endTime}" +
            "   </if>" +
            "   <if test='status != null'>" +
            "       <choose>" +
            "           <when test='status == 1'>AND status = 1 AND is_deleted = 0</when>" +
            "           <when test='status == 0'>AND status = 0 AND is_deleted = 0</when>" +
            "           <when test='status == 2'>AND is_deleted = 1</when>" +
            "       </choose>" +
            "   </if>" +
            "</where>" +
            " ORDER BY id DESC" +
            "</script>")
    List<User> query(UserQueryParam param);

    // 修改用户状态
    @Update("UPDATE user SET status = #{status} WHERE id = #{id} AND is_deleted = 0")
    void updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    // 根据用户名查询（用于注册查重、登录）
    @Select("SELECT id, username, password, nickname, status, is_deleted, role FROM user WHERE username = #{username}")
    User getByUsername(String username);

    // 新增用户（注册）
    @Insert("INSERT INTO user(username, password, nickname, status, is_deleted, create_time, update_time) " +
            "VALUES(#{username}, #{password}, #{nickname}, 1, 0, NOW(), NOW())")
    void insert(User user);

    // 根据ID查询用户（删除了 hobby）
    @Select("SELECT id, username, password, nickname, avatar, signature, gender, region, status, is_deleted, create_time, update_time, role FROM user WHERE id = #{id}")
    User getById(Integer id);

    // 更新个人信息（不包含密码、状态、角色，也没有 hobby）
    @Update("UPDATE user SET nickname = #{nickname}, avatar = #{avatar}, signature = #{signature}, " +
            "gender = #{gender}, region = #{region}, update_time = NOW() WHERE id = #{id}")
    void updateProfile(User user);
}