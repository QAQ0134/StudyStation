package com.studystation.mapper;

import com.studystation.pojo.KnowledgePoint;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface KnowledgePointMapper {

    // 联表查询所有知识点，并带上学科名称
    @Select("SELECT kp.id, kp.name, kp.subject_area_id AS subjectAreaId, sa.name AS subjectAreaName " +
            "FROM knowledge_point kp LEFT JOIN subject_area sa ON kp.subject_area_id = sa.id")
    List<KnowledgePoint> findAll();

    // 查重：判断同一个学科下是否有同名知识点
    @Select("SELECT * FROM knowledge_point WHERE name = #{name} AND subject_area_id = #{subjectAreaId}")
    KnowledgePoint getByNameAndSubjectId(@Param("name") String name, @Param("subjectAreaId") Integer subjectAreaId);

    // 根据学科ID查询知识点
    @Select("SELECT kp.id, kp.name, kp.subject_area_id AS subjectAreaId, sa.name AS subjectAreaName " +
            "FROM knowledge_point kp LEFT JOIN subject_area sa ON kp.subject_area_id = sa.id " +
            "WHERE kp.subject_area_id = #{subjectAreaId}")
    List<KnowledgePoint> findBySubjectAreaId(Integer subjectAreaId);

    // 新增
    @Insert("INSERT INTO knowledge_point(name, subject_area_id) VALUES(#{name}, #{subjectAreaId})")
    void insert(KnowledgePoint knowledgePoint);

    // 修改
    @Update("UPDATE knowledge_point SET name = #{name}, subject_area_id = #{subjectAreaId} WHERE id = #{id}")
    void update(KnowledgePoint knowledgePoint);

    // 删除
    @Delete("DELETE FROM knowledge_point WHERE id = #{id}")
    void deleteById(Integer id);
}