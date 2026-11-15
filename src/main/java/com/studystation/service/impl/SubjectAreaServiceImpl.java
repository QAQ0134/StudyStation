package com.studystation.service.impl;

import com.studystation.mapper.SubjectAreaMapper;
import com.studystation.pojo.SubjectArea;
import com.studystation.service.SubjectAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectAreaServiceImpl implements SubjectAreaService {

    @Autowired
    private SubjectAreaMapper subjectAreaMapper;

    @Override
    public List<SubjectArea> findAll() {
        return subjectAreaMapper.findAll();
    }

    @Override
    public String add(SubjectArea subjectArea) {
        // 1. 校验名称是否为空
        if (subjectArea.getName() == null || subjectArea.getName().trim().isEmpty()) {
            return "学科名称不能为空";
        }

        // 2. 查重
        SubjectArea exist = subjectAreaMapper.getByName(subjectArea.getName().trim());
        if (exist != null) {
            return "该学科已存在，不能重复添加";
        }

        // 3. 执行新增
        subjectAreaMapper.insert(subjectArea);
        return null; // 返回 null 代表成功
    }

    @Override
    public String update(SubjectArea subjectArea) {
        // 修改时也需要查重（排除自己）
        SubjectArea exist = subjectAreaMapper.getByName(subjectArea.getName().trim());
        if (exist != null && !exist.getId().equals(subjectArea.getId())) {
            return "该学科名称已被其他数据使用";
        }
        subjectAreaMapper.update(subjectArea);
        return null;
    }

    @Override
    public void deleteById(Integer id) {
        subjectAreaMapper.deleteById(id);
    }
}
