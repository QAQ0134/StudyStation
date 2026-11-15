package com.studystation.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserHobbyMapper {

    // 查询用户的所有爱好知识点ID
    @Select("SELECT knowledge_point_id FROM user_hobby WHERE user_id = #{userId}")
    List<Integer> getHobbyIdsByUserId(Integer userId);

    // 删除用户的所有爱好（用于修改时先清空）
    @Delete("DELETE FROM user_hobby WHERE user_id = #{userId}")
    void deleteByUserId(Integer userId);

    // 批量插入用户爱好（可以循环调用，或者用 foreach 批量插入）
    @Insert("INSERT INTO user_hobby(user_id, knowledge_point_id) VALUES(#{userId}, #{knowledgePointId})")
    void insertHobby(@Param("userId") Integer userId, @Param("knowledgePointId") Integer knowledgePointId);
}