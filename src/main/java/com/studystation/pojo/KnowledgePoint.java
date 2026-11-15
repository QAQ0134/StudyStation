package com.studystation.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePoint {
    private Integer id;
    private String name;
    private Integer subjectAreaId; // 对应数据库的 subject_area_id
    private String subjectAreaName;
}