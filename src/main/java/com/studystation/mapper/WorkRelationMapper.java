package com.studystation.mapper;

import com.studystation.pojo.KnowledgePointTag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WorkRelationMapper {

    // 插入作品-学科关联
    @Insert("INSERT INTO work_subject(work_id, subject_area_id) VALUES(#{workId}, #{subjectId})")
    void insertWorkSubject(@Param("workId") Integer workId, @Param("subjectId") Integer subjectId);

    // 插入作品-知识点关联
    @Insert("INSERT INTO work_knowledge_point(work_id, knowledge_point_id) VALUES(#{workId}, #{kpId})")
    void insertWorkKp(@Param("workId") Integer workId, @Param("kpId") Integer kpId);

    // 根据作品ID查询所属学科IDs
    @Select("SELECT subject_area_id FROM work_subject WHERE work_id = #{workId}")
    List<Integer> getSubjectIdsByWorkId(Integer workId);

    // 根据作品ID查询关联知识点IDs
    @Select("SELECT knowledge_point_id FROM work_knowledge_point WHERE work_id = #{workId}")
    List<Integer> getKpIdsByWorkId(Integer workId);

    // 根据作品ID查询关联的知识点详细信息（ID和名称）
    @Select("SELECT kp.id, kp.name " +
            "FROM work_knowledge_point wk " +
            "JOIN knowledge_point kp ON wk.knowledge_point_id = kp.id " +
            "WHERE wk.work_id = #{workId}")
    List<KnowledgePointTag> getKpTagsByWorkId(Integer workId);

    // 删除作品-学科关联
    @Delete("DELETE FROM work_subject WHERE work_id = #{workId}")
    void deleteSubjectsByWorkId(Integer workId);

    // 删除作品-知识点关联
    @Delete("DELETE FROM work_knowledge_point WHERE work_id = #{workId}")
    void deleteKpsByWorkId(Integer workId);
}