package com.studystation.service.impl;

import com.studystation.mapper.KnowledgePointMapper;
import com.studystation.pojo.KnowledgePoint;
import com.studystation.service.KnowledgePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgePointServiceImpl implements KnowledgePointService {

    @Autowired
    private KnowledgePointMapper knowledgePointMapper;

    @Override
    public List<KnowledgePoint> findAll() {
        return knowledgePointMapper.findAll();
    }

    @Override
    public String add(KnowledgePoint knowledgePoint) {
        if (knowledgePoint.getName() == null || knowledgePoint.getSubjectAreaId() == null) {
            return "知识点名称或所属学科不能为空";
        }
        // 校验：同一学科下不能有同名知识点
        KnowledgePoint exist = knowledgePointMapper.getByNameAndSubjectId(knowledgePoint.getName().trim(), knowledgePoint.getSubjectAreaId());
        if (exist != null) {
            return "该学科下已存在同名知识点";
        }
        knowledgePointMapper.insert(knowledgePoint);
        return null;
    }

    @Override
    public List<KnowledgePoint> findBySubjectAreaId(Integer subjectAreaId) {
        return knowledgePointMapper.findBySubjectAreaId(subjectAreaId);
    }

    @Override
    public String update(KnowledgePoint knowledgePoint) {
        KnowledgePoint exist = knowledgePointMapper.getByNameAndSubjectId(knowledgePoint.getName().trim(), knowledgePoint.getSubjectAreaId());
        // 如果查到了同名记录，且不是当前正在修改的这条记录，说明重名了
        if (exist != null && !exist.getId().equals(knowledgePoint.getId())) {
            return "该学科下已存在同名知识点";
        }
        knowledgePointMapper.update(knowledgePoint);
        return null;
    }

    @Override
    public void deleteById(Integer id) {
        knowledgePointMapper.deleteById(id);
    }
}