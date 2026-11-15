package com.studystation.service;

import com.studystation.pojo.KnowledgePoint;
import java.util.List;

public interface KnowledgePointService {
    List<KnowledgePoint> findAll();
    String add(KnowledgePoint knowledgePoint);
    List<KnowledgePoint> findBySubjectAreaId(Integer subjectAreaId);
    String update(KnowledgePoint knowledgePoint);
    void deleteById(Integer id);
}