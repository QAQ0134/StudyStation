package com.studystation.mapper;

import com.studystation.pojo.SubjectArea;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SubjectAreaMapper {

    // 查询
    @Select("select id, name from subject_area")
    List<SubjectArea> findAll();

    // 根据名称查询（用于查重）
    @Select("select id, name from subject_area where name = #{name}")
    SubjectArea getByName(String name);

    // 新增
    @Insert("insert into subject_area(name) values(#{name})")
    void insert(SubjectArea subjectArea);

    // 修改
    @Update("update subject_area set name = #{name} where id = #{id}")
    void update(SubjectArea subjectArea);

    // 删除
    @Delete("delete from subject_area where id = #{id}")
    void deleteById(Integer id);
}
