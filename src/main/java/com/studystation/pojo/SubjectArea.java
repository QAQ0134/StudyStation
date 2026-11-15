package com.studystation.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 学科领域
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectArea {
    private Integer id;
    private String name;
    private List<KnowledgePoint> knowledgePoints;
}
