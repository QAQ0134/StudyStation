package com.studystation.mapper;

import com.studystation.pojo.OperationLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OperationLogMapper {

    @Insert("INSERT INTO operation_log(username, module, type, description, ip, create_time) " +
            "VALUES(#{username}, #{module}, #{type}, #{description}, #{ip}, NOW())")
    void insertLog(OperationLog log);

    // 动态分页查询
    @Select("<script>" +
            "SELECT * FROM operation_log " +
            "<where>" +
            "   <if test='type != null and type != \"\"'>" +
            "       AND type = #{type}" +
            "   </if>" +
            "   <if test='description != null and description != \"\"'>" +
            "       AND description LIKE CONCAT('%', #{description}, '%')" +
            "   </if>" +
            "</where>" +
            " ORDER BY create_time DESC" + // 按时间倒序
            "</script>")
    List<OperationLog> queryLogs(@Param("type") String type, @Param("description") String description);
}